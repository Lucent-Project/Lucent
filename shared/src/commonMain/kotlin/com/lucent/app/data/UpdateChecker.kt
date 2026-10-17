package com.lucent.app.data

import com.lucent.app.LucentBuild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull


@Serializable
data class ReleaseAsset(
    val name: String,
    val url: String,
    val sizeBytes: Long,
    val fallbackDownloadUrl: String? = null
)

@Serializable
data class ReleaseInfo(
    val tag: String,
    val version: String,
    val title: String,
    val apk: ReleaseAsset?,
    val installer: ReleaseAsset?,
    val notesUrl: String = "",
    val channel: String = "stable",
    val apkSha: String? = null,
    val exeSha: String? = null,
    val apkVersion: String? = null,
    val exeVersion: String? = null,
    val buildId: String? = null
) {
    val identity: String
        get() = version

    val releaseUrl: String
        get() = notesUrl.ifBlank { LucentBuild.HOMEPAGE + "/releases/tag/" + tag }
}

object UpdateChecker {

    const val MIRROR_MANIFEST_URL = "https://lucent-project.github.io/lucentapp/downloads/version.json"
    const val GITHUB_LATEST_URL = "https://api.github.com/repos/Lucent-Project/Lucent/releases/latest"

    internal val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(java.time.Duration.ofSeconds(20))
            .readTimeout(java.time.Duration.ofSeconds(30))
            .build()
    }

    fun parseVersion(text: String): List<Int>? {
        val cleaned = text.trim().trimStart('v', 'V')
        val core = cleaned.substringBefore('-').substringBefore('+')
        val parts = core.split('.')
        if (parts.isEmpty() || parts.size > 4) return null
        val numbers = parts.map { it.toIntOrNull() ?: return null }
        if (numbers.any { it < 0 }) return null
        return numbers
    }

    fun isNewer(candidate: String, current: String): Boolean {
        val a = parseVersion(candidate) ?: return false
        val b = parseVersion(current) ?: return false
        val size = maxOf(a.size, b.size)
        for (i in 0 until size) {
            val left = a.getOrElse(i) { 0 }
            val right = b.getOrElse(i) { 0 }
            if (left != right) return left > right
        }
        return false
    }

    fun parseBuildId(id: String): Pair<Char, Long>? {
        if (id.length != 13) return null
        val prefix = id[0]
        if (prefix != 'R' && prefix != 'P') return null
        val timestamp = id.substring(1).toLongOrNull() ?: return null
        return prefix to timestamp
    }

    fun isNewerBuildId(candidate: String, current: String): Boolean {
        val candParsed = parseBuildId(candidate) ?: return false
        val currParsed = parseBuildId(current) ?: return false
        return candParsed.second > currParsed.second
    }

    fun buildIdTrack(id: String): String {
        return when (id.firstOrNull()) {
            'P' -> "preview"
            'R' -> "official"
            else -> "dev"
        }
    }

    private suspend fun fetchJson(url: String, timeoutSeconds: Long = 20): JsonObject? = withContext(Dispatchers.IO) {
        val httpClient = client.newBuilder()
            .callTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
            .connectTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
            .readTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Lucent")
            .build()
        val body = runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()
            }
        }.getOrNull() ?: return@withContext null
        if (body.isBlank()) return@withContext null
        runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()
    }

    private suspend fun fetchTagCommit(tag: String): String? {
        val ref = fetchJson(LucentBuild.REPO_API + "/git/ref/tags/" + tag) ?: return null
        val obj = ref["object"]?.jsonObject ?: return null
        val sha = obj["sha"]?.jsonPrimitive?.content ?: ""
        if (sha.isBlank()) return null
        if (obj["type"]?.jsonPrimitive?.content == "tag") {
            val tagObject = fetchJson(LucentBuild.REPO_API + "/git/tags/" + sha) ?: return null
            return tagObject["object"]?.jsonObject?.get("sha")?.jsonPrimitive?.content?.ifBlank { null }
        }
        return sha
    }

    private fun parseAssets(root: JsonObject, tagName: String = ""): Pair<ReleaseAsset?, ReleaseAsset?> {
        var apk: ReleaseAsset? = null
        var installer: ReleaseAsset? = null
        val assets = root["assets"]?.jsonArray ?: return null to null
        for (i in 0 until assets.size) {
            val item = assets[i] as? JsonObject ?: continue
            val name = item["name"]?.jsonPrimitive?.content ?: ""
            val url = item["browser_download_url"]?.jsonPrimitive?.content ?: ""
            if (name.isBlank() || url.isBlank()) continue
            val fallbackUrl = item["fallback_download_url"]?.jsonPrimitive?.content?.ifBlank { null }
                ?: if (tagName.isNotBlank()) "https://github.com/Lucent-Project/Lucent/releases/download/$tagName/$name" else null
            val asset = ReleaseAsset(name, url, item["size"]?.jsonPrimitive?.longOrNull ?: 0L, fallbackUrl)
            when {
                name.endsWith(".apk", ignoreCase = true) && apk == null -> apk = asset
                name.endsWith(".exe", ignoreCase = true) && installer == null -> installer = asset
            }
        }
        return apk to installer
    }

    private fun parseRelease(root: JsonObject): Pair<ReleaseInfo, String>? {
        val tag = root["tag_name"]?.jsonPrimitive?.content ?: ""
        val isDraftOrPrerelease = (root["draft"]?.jsonPrimitive?.booleanOrNull == true) || (root["prerelease"]?.jsonPrimitive?.booleanOrNull == true)
        if (tag.isBlank() || isDraftOrPrerelease) return null
        val body = root["body"]?.jsonPrimitive?.content ?: ""
        val buildId = Regex("(?m)^(?:-\\s*)?Build-ID:\\s*(\\S+)").find(body)?.groupValues?.get(1)?.ifBlank { null }
        val (apk, installer) = parseAssets(root, tag)
        val info = ReleaseInfo(
            tag = tag,
            version = tag.trimStart('v', 'V'),
            title = root["name"]?.jsonPrimitive?.content?.ifBlank { tag } ?: tag,
            apk = apk,
            installer = installer,
            notesUrl = root["html_url"]?.jsonPrimitive?.content ?: "",
            channel = "stable",
            buildId = buildId
        )
        return info to tag
    }

    private suspend fun fetchLatestRelease(): Pair<ReleaseInfo, String>? = withContext(Dispatchers.IO) {
        val mirror = runCatching { fetchJson(MIRROR_MANIFEST_URL, 8)?.let { parseRelease(it) } }.getOrNull()
        if (mirror != null) return@withContext mirror
        val github = runCatching { fetchJson(GITHUB_LATEST_URL, 12)?.let { parseRelease(it) } }.getOrNull()
        github
    }

    suspend fun latest(currentVersion: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        var stable: ReleaseInfo? = null
        var stableTag = ""
        val release = fetchLatestRelease()
        if (release != null) {
            val (info, tag) = release
            stableTag = tag
            val buildId = info.buildId
            val cBuildId = LucentBuild.BUILD_ID
            val usesBuildId = buildId != null && parseBuildId(buildId) != null && parseBuildId(cBuildId) != null
            val isNewerStable = if (usesBuildId) {
                isNewerBuildId(buildId, cBuildId)
            } else {
                isNewer(tag, currentVersion)
            }
            if (isNewerStable) {
                stable = info
            }
        }

        var candidate: ReleaseInfo? = null
        if (SettingsCache.updateChannel == "preview") {
            val preview = PreviewChecker.getPreviewCandidate(client)
            if (preview != null) {
                val pBuildId = preview.buildId
                val cBuildId = LucentBuild.BUILD_ID
                val usesBuildId = pBuildId != null && parseBuildId(pBuildId) != null && parseBuildId(cBuildId) != null
                val isNewerPreview = if (usesBuildId) {
                    isNewerBuildId(pBuildId, cBuildId)
                } else {
                    isNewer(AutoUpdate.installer?.versionOf(preview) ?: preview.version, currentVersion)
                }
                if (isNewerPreview) candidate = preview
            }
        } else {
            candidate = stable
        }

        var result = candidate ?: return@withContext null
        val engine = AutoUpdate.installer
        if (engine != null && !engine.hasAsset(result)) {
            val fallback = stable
            if (result.channel == "preview" && fallback != null && engine.hasAsset(fallback)) {
                result = fallback
            } else {
                return@withContext null
            }
        }
        val candidateVersion = engine?.versionOf(result) ?: result.version
        val candidateIdentity = engine?.identityOf(result) ?: result.identity
        val installedIdentity = SettingsCache.installedPreviewIdentity
        if (installedIdentity.isNotBlank() && candidateIdentity == installedIdentity) return@withContext null
        if (candidateIdentity == currentVersion) return@withContext null
        if (result.channel == "preview") {
            if (parseVersion(candidateVersion) == null) return@withContext null
            if (isNewer(currentVersion, candidateVersion)) return@withContext null
            if (candidateVersion == currentVersion && stableTag.isNotBlank()) {
                val tagCommit = fetchTagCommit(stableTag)
                if (tagCommit != null && candidateIdentity == tagCommit) return@withContext null
            }
        }
        result
    }
}
