package com.lucent.app.harness

import com.lucent.app.network.ToolExecResult
import com.lucent.app.platform.PlatformContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okio.FileSystem
import okio.Path.Companion.toPath

enum class SelfTestStatus {
    PASS,
    BLOCKED_DENY,
    CONFIRMED_PASS,
    BLOCKED_DECLINED,
    FAIL
}

data class SelfTestOutcome(
    val permission: HarnessPermission,
    val policy: Approval,
    val status: SelfTestStatus,
    val toolName: String,
    val evidence: String,
    val durationMs: Long
) {
    val matchesPolicy: Boolean
        get() = when (policy) {
            Approval.ALLOW -> status == SelfTestStatus.PASS
            Approval.DENY -> status == SelfTestStatus.BLOCKED_DENY
            Approval.CONFIRM -> status == SelfTestStatus.CONFIRMED_PASS || status == SelfTestStatus.BLOCKED_DECLINED
        }
}

object PermissionSelfTest {

    private const val DEFAULT_WEB_PROBE_URL = "https://example.com"

    private val mutex = Mutex()
    private val seqLock = Any()
    private var seqCounter = 0L

    private fun nextNonce(): String {
        val n = withHarnessLock(seqLock) {
            seqCounter += 1L
            seqCounter
        }
        return "${harnessCurrentTimeMillis()}-$n"
    }

    fun backingGroups(permission: HarnessPermission): Set<HarnessGroup> = when (permission) {
        HarnessPermission.READ -> setOf(HarnessGroup.FILES)
        HarnessPermission.WRITE -> setOf(HarnessGroup.FILES)
        HarnessPermission.DELETE -> setOf(HarnessGroup.FILES)
        HarnessPermission.EXECUTE -> setOf(HarnessGroup.TERMINAL)
        HarnessPermission.NETWORK -> setOf(HarnessGroup.CONNECTORS)
        HarnessPermission.GIT -> setOf(HarnessGroup.GIT)
        HarnessPermission.GITHUB -> setOf(HarnessGroup.GITHUB)
        HarnessPermission.BROWSER -> setOf(HarnessGroup.BROWSER)
        HarnessPermission.SENSITIVE -> setOf(HarnessGroup.FILES)
        HarnessPermission.DEVICE -> setOf(HarnessGroup.DEVICE)
    }

    fun syncConfigForPermission(
        config: HarnessConfig,
        permission: HarnessPermission,
        approval: Approval
    ): HarnessConfig {
        var next = config.withApproval(permission, approval)
        if (approval != Approval.DENY) {
            backingGroups(permission).forEach { group ->
                next = next.withGroup(group, true)
            }
            if (permission == HarnessPermission.DEVICE) {
                next = next.copy(deviceEnabled = true)
            }
        }
        return next
    }

    suspend fun runAll(
        context: PlatformContext,
        httpProbeUrl: String? = null,
        onProgress: ((HarnessPermission) -> Unit)? = null,
        confirm: (suspend (HarnessPermission, String) -> Boolean)? = null
    ): List<SelfTestOutcome> {
        val results = mutableListOf<SelfTestOutcome>()
        for (permission in HarnessPermission.entries) {
            onProgress?.invoke(permission)
            results.add(runOne(context, permission, httpProbeUrl, confirm))
        }
        return results
    }

    suspend fun runOne(
        context: PlatformContext,
        permission: HarnessPermission,
        httpProbeUrl: String? = null,
        confirm: (suspend (HarnessPermission, String) -> Boolean)? = null
    ): SelfTestOutcome = mutex.withLock {
        val savedConfig = HarnessRuntime.config()
        val policy = savedConfig.approvalFor(permission)
        val started = harnessCurrentTimeMillis()
        val fs = systemHarnessFs()
        val nonce = nextNonce()
        val cleanupPaths = mutableListOf<String>()

        var askedConfirm = false
        var userApprovedConfirm = false

        val gateConfirm: suspend (HarnessTool, HarnessPermission, String) -> Boolean = { _, perm, desc ->
            if (perm == permission) {
                askedConfirm = true
                val decision = confirm?.invoke(perm, redactSecrets(desc, savedConfig)) ?: false
                userApprovedConfirm = decision
                decision
            } else {
                true
            }
        }

        val probeSpec = try {
            prepareProbe(permission, nonce, fs, savedConfig, httpProbeUrl, cleanupPaths)
        } catch (t: Throwable) {
            cleanupAll(cleanupPaths)
            val elapsed = (harnessCurrentTimeMillis() - started).coerceAtLeast(0L)
            return@withLock SelfTestOutcome(
                permission = permission,
                policy = policy,
                status = SelfTestStatus.FAIL,
                toolName = toolNameFor(permission),
                evidence = redactSecrets(t.message ?: "Probe setup failed", savedConfig),
                durationMs = elapsed
            )
        }

        val probeConfig = buildProbeConfig(savedConfig, permission, policy, probeSpec.outsideDir)
        HarnessRuntime.install(probeConfig)
        try {
            val execResult = HarnessGate.execute(
                context = context,
                db = null,
                name = probeSpec.toolName,
                argumentsJson = probeSpec.argumentsJson,
                confirm = gateConfirm
            )
            val elapsed = (harnessCurrentTimeMillis() - started).coerceAtLeast(0L)
            classifyOutcome(
                permission = permission,
                policy = policy,
                probeSpec = probeSpec,
                execResult = execResult,
                askedConfirm = askedConfirm,
                userApprovedConfirm = userApprovedConfirm,
                savedConfig = savedConfig,
                durationMs = elapsed
            )
        } catch (t: Throwable) {
            val elapsed = (harnessCurrentTimeMillis() - started).coerceAtLeast(0L)
            SelfTestOutcome(
                permission = permission,
                policy = policy,
                status = SelfTestStatus.FAIL,
                toolName = probeSpec.toolName,
                evidence = redactSecrets(t.message ?: "Self-test threw an unexpected error", savedConfig),
                durationMs = elapsed
            )
        } finally {
            HarnessRuntime.install(savedConfig)
            cleanupAll(cleanupPaths)
        }
    }

    private data class ProbeSpec(
        val toolName: String,
        val argumentsJson: String,
        val verifyPass: (ToolExecResult) -> String?,
        val verifyBlocked: (ToolExecResult) -> Boolean = { true },
        val outsideDir: String? = null
    )

    private fun toolNameFor(permission: HarnessPermission): String = when (permission) {
        HarnessPermission.READ -> "read_file"
        HarnessPermission.WRITE -> "write_file"
        HarnessPermission.DELETE -> "delete_path"
        HarnessPermission.EXECUTE -> "run_command"
        HarnessPermission.NETWORK -> "http_request"
        HarnessPermission.GIT -> "git_init"
        HarnessPermission.GITHUB -> "github_branches"
        HarnessPermission.BROWSER -> "fetch_url"
        HarnessPermission.SENSITIVE -> "read_file"
        HarnessPermission.DEVICE -> "device_info"
    }

    private fun buildProbeConfig(
        saved: HarnessConfig,
        permission: HarnessPermission,
        policy: Approval,
        outsideDir: String?
    ): HarnessConfig {
        var cfg = saved.copy(enabled = true, snapshots = false)
        if (permission == HarnessPermission.SENSITIVE) {
            cfg = cfg
                .withGroup(HarnessGroup.FILES, true)
                .withApproval(HarnessPermission.READ, Approval.ALLOW)
                .withApproval(HarnessPermission.SENSITIVE, policy)
            if (outsideDir != null) {
                val filteredWrite = cfg.writeRoots.filterNot { root ->
                    Workspace.isInside(outsideDir, root)
                }
                val filteredRead = cfg.readOnlyRoots.filterNot { root ->
                    Workspace.isInside(outsideDir, root)
                }
                cfg = cfg.copy(writeRoots = filteredWrite, readOnlyRoots = filteredRead)
            }
        }
        return cfg
    }

    private fun prepareProbe(
        permission: HarnessPermission,
        nonce: String,
        fs: HarnessFs,
        savedConfig: HarnessConfig,
        httpProbeUrl: String?,
        cleanupPaths: MutableList<String>
    ): ProbeSpec {
        val workspace = HarnessRuntime.workspacePath()
        fs.mkdirs(workspace)
        return when (permission) {
            HarnessPermission.READ -> {
                val name = ".lucent-perm-read-$nonce.tmp"
                val full = fs.join(workspace, name)
                val payload = "lucent-read-probe-$nonce"
                cleanupPaths.add(full)
                fs.writeText(full, payload)
                ProbeSpec(
                    toolName = "read_file",
                    argumentsJson = buildJsonObject { put("path", name) }.toString(),
                    verifyPass = { res ->
                        if (res.success && res.summary.contains(payload)) {
                            "read_file read $name (${payload.length} B verified)"
                        } else null
                    }
                )
            }
            HarnessPermission.WRITE -> {
                val name = ".lucent-perm-write-$nonce.tmp"
                val full = fs.join(workspace, name)
                val payload = "lucent-write-probe-$nonce"
                cleanupPaths.add(full)
                ProbeSpec(
                    toolName = "write_file",
                    argumentsJson = buildJsonObject {
                        put("path", name)
                        put("content", payload)
                    }.toString(),
                    verifyPass = { res ->
                        val onDisk = runCatching { fs.exists(full) && fs.readBytes(full).decodeToString() == payload }
                            .getOrDefault(false)
                        if (res.success && onDisk) {
                            "write_file wrote $name (${payload.length} B verified on disk)"
                        } else null
                    },
                    verifyBlocked = { !fs.exists(full) }
                )
            }
            HarnessPermission.DELETE -> {
                val name = ".lucent-perm-delete-$nonce.tmp"
                val full = fs.join(workspace, name)
                val payload = "lucent-delete-probe-$nonce"
                cleanupPaths.add(full)
                fs.writeText(full, payload)
                ProbeSpec(
                    toolName = "delete_path",
                    argumentsJson = buildJsonObject { put("path", name) }.toString(),
                    verifyPass = { res ->
                        if (res.success && !fs.exists(full)) {
                            "delete_path removed temp file $name (verified absent)"
                        } else null
                    },
                    verifyBlocked = { fs.exists(full) }
                )
            }
            HarnessPermission.EXECUTE -> {
                val marker = "lucent-exec-ok-$nonce"
                ProbeSpec(
                    toolName = "run_command",
                    argumentsJson = buildJsonObject { put("command", "echo $marker") }.toString(),
                    verifyPass = { res ->
                        if (res.success && res.summary.contains(marker)) {
                            "run_command echoed $marker (exit 0)"
                        } else null
                    }
                )
            }
            HarnessPermission.NETWORK -> {
                val url = httpProbeUrl?.takeIf { it.isNotBlank() } ?: DEFAULT_WEB_PROBE_URL
                val hostLabel = hostLabelOf(url)
                ProbeSpec(
                    toolName = "http_request",
                    argumentsJson = buildJsonObject {
                        put("method", "GET")
                        put("url", url)
                    }.toString(),
                    verifyPass = { res ->
                        val firstLine = res.summary.lineSequence().firstOrNull().orEmpty().trim()
                        if (res.success && firstLine.startsWith("HTTP ")) {
                            "http_request GET $hostLabel -> $firstLine"
                        } else null
                    }
                )
            }
            HarnessPermission.GIT -> {
                val dirName = ".lucent-perm-git-$nonce"
                val fullDir = fs.join(workspace, dirName)
                val dotGit = fs.join(fullDir, ".git")
                cleanupPaths.add(fullDir)
                ProbeSpec(
                    toolName = "git_init",
                    argumentsJson = buildJsonObject { put("repo", dirName) }.toString(),
                    verifyPass = { res ->
                        if (res.success && fs.exists(dotGit)) {
                            "git_init initialized isolated temp repo $dirName/.git"
                        } else null
                    },
                    verifyBlocked = { !fs.exists(dotGit) }
                )
            }
            HarnessPermission.GITHUB -> {
                val apiLabel = hostLabelOf(savedConfig.githubApi.ifBlank { "https://api.github.com" })
                ProbeSpec(
                    toolName = "github_branches",
                    argumentsJson = buildJsonObject {
                        put("action", "rate_limit")
                        put("anonymous", true)
                    }.toString(),
                    verifyPass = { res ->
                        val firstLine = res.summary.lineSequence().firstOrNull().orEmpty().trim()
                        if (res.success && firstLine.isNotEmpty()) {
                            "github_branches ($apiLabel) -> $firstLine"
                        } else null
                    }
                )
            }
            HarnessPermission.BROWSER -> {
                val url = httpProbeUrl?.takeIf { it.isNotBlank() } ?: DEFAULT_WEB_PROBE_URL
                val hostLabel = hostLabelOf(url)
                ProbeSpec(
                    toolName = "fetch_url",
                    argumentsJson = buildJsonObject {
                        put("url", url)
                        put("max_chars", 400)
                    }.toString(),
                    verifyPass = { res ->
                        val head = res.summary.lineSequence().firstOrNull().orEmpty().trim().take(72)
                        if (res.success && head.isNotEmpty()) {
                            "fetch_url $hostLabel -> $head"
                        } else null
                    }
                )
            }
            HarnessPermission.SENSITIVE -> {
                val cacheCandidate = fs.join(HarnessRuntime.cacheDirPath(), "perm-selftest")
                val outsideDir = if (!Workspace.isInside(cacheCandidate, workspace)) {
                    cacheCandidate
                } else {
                    fs.join(harnessTempDir(), "lucent-perm-selftest-$nonce")
                }
                fs.mkdirs(outsideDir)
                val outsideFile = fs.join(outsideDir, "outside-$nonce.tmp")
                val payload = "lucent-outside-probe-$nonce"
                cleanupPaths.add(outsideFile)
                if (outsideDir != cacheCandidate) cleanupPaths.add(outsideDir)
                fs.writeText(outsideFile, payload)
                ProbeSpec(
                    toolName = "read_file",
                    argumentsJson = buildJsonObject { put("path", outsideFile) }.toString(),
                    verifyPass = { res ->
                        if (res.success && res.summary.contains(payload)) {
                            "read_file read outside-workspace temp file (${payload.length} B verified)"
                        } else null
                    },
                    outsideDir = outsideDir
                )
            }
            HarnessPermission.DEVICE -> {
                ProbeSpec(
                    toolName = "device_info",
                    argumentsJson = "{}",
                    verifyPass = { res ->
                        val snippet = res.summary.lines()
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString(" · ")
                            .take(96)
                        if (res.success && snippet.isNotEmpty()) {
                            "device_info -> $snippet"
                        } else null
                    }
                )
            }
        }
    }

    private fun classifyOutcome(
        permission: HarnessPermission,
        policy: Approval,
        probeSpec: ProbeSpec,
        execResult: ToolExecResult,
        askedConfirm: Boolean,
        userApprovedConfirm: Boolean,
        savedConfig: HarnessConfig,
        durationMs: Long
    ): SelfTestOutcome {
        val rawSummary = redactSecrets(execResult.summary.lineSequence().firstOrNull().orEmpty().trim(), savedConfig)
        return when (policy) {
            Approval.DENY -> {
                val blockedByPolicy = !execResult.success &&
                    execResult.summary.contains("blocked by the permission policy", ignoreCase = true) &&
                    probeSpec.verifyBlocked(execResult)
                if (blockedByPolicy) {
                    SelfTestOutcome(
                        permission = permission,
                        policy = policy,
                        status = SelfTestStatus.BLOCKED_DENY,
                        toolName = probeSpec.toolName,
                        evidence = rawSummary,
                        durationMs = durationMs
                    )
                } else {
                    SelfTestOutcome(
                        permission = permission,
                        policy = policy,
                        status = SelfTestStatus.FAIL,
                        toolName = probeSpec.toolName,
                        evidence = rawSummary.ifEmpty { "${probeSpec.toolName} was not blocked under Deny" },
                        durationMs = durationMs
                    )
                }
            }
            Approval.ALLOW -> {
                val verified = probeSpec.verifyPass(execResult)
                if (verified != null && !askedConfirm) {
                    SelfTestOutcome(
                        permission = permission,
                        policy = policy,
                        status = SelfTestStatus.PASS,
                        toolName = probeSpec.toolName,
                        evidence = redactSecrets(verified, savedConfig),
                        durationMs = durationMs
                    )
                } else {
                    SelfTestOutcome(
                        permission = permission,
                        policy = policy,
                        status = SelfTestStatus.FAIL,
                        toolName = probeSpec.toolName,
                        evidence = rawSummary.ifEmpty { "${probeSpec.toolName} did not complete under Allow" },
                        durationMs = durationMs
                    )
                }
            }
            Approval.CONFIRM -> {
                if (!askedConfirm) {
                    SelfTestOutcome(
                        permission = permission,
                        policy = policy,
                        status = SelfTestStatus.FAIL,
                        toolName = probeSpec.toolName,
                        evidence = rawSummary.ifEmpty { "${probeSpec.toolName} did not request confirmation" },
                        durationMs = durationMs
                    )
                } else if (userApprovedConfirm) {
                    val verified = probeSpec.verifyPass(execResult)
                    if (verified != null) {
                        SelfTestOutcome(
                            permission = permission,
                            policy = policy,
                            status = SelfTestStatus.CONFIRMED_PASS,
                            toolName = probeSpec.toolName,
                            evidence = redactSecrets(verified, savedConfig),
                            durationMs = durationMs
                        )
                    } else {
                        SelfTestOutcome(
                            permission = permission,
                            policy = policy,
                            status = SelfTestStatus.FAIL,
                            toolName = probeSpec.toolName,
                            evidence = rawSummary.ifEmpty { "${probeSpec.toolName} failed after confirmation" },
                            durationMs = durationMs
                        )
                    }
                } else {
                    val properlyDeclined = !execResult.success &&
                        execResult.summary.contains("not confirmed", ignoreCase = true) &&
                        probeSpec.verifyBlocked(execResult)
                    if (properlyDeclined) {
                        SelfTestOutcome(
                            permission = permission,
                            policy = policy,
                            status = SelfTestStatus.BLOCKED_DECLINED,
                            toolName = probeSpec.toolName,
                            evidence = rawSummary,
                            durationMs = durationMs
                        )
                    } else {
                        SelfTestOutcome(
                            permission = permission,
                            policy = policy,
                            status = SelfTestStatus.FAIL,
                            toolName = probeSpec.toolName,
                            evidence = rawSummary.ifEmpty { "${probeSpec.toolName} was not blocked when declined" },
                            durationMs = durationMs
                        )
                    }
                }
            }
        }
    }

    fun redactSecrets(text: String, config: HarnessConfig = HarnessRuntime.config()): String {
        if (text.isEmpty()) return text
        var out = text
        val secrets = buildList {
            if (config.githubToken.trim().length >= 4) add(config.githubToken.trim())
            config.githubTokens.forEach { if (it.token.trim().length >= 4) add(it.token.trim()) }
            config.connectors.forEach { if (it.token.trim().length >= 4) add(it.token.trim()) }
            config.mcpServers.forEach { if (it.token.trim().length >= 4) add(it.token.trim()) }
        }.distinct().sortedByDescending { it.length }
        secrets.forEach { secret ->
            out = out.replace(secret, "[REDACTED]")
        }
        out = out.replace(Regex("gh[pousr]_[A-Za-z0-9_]{8,}"), "[REDACTED]")
        out = out.replace(Regex("github_pat_[A-Za-z0-9_]{8,}"), "[REDACTED]")
        out = out.replace(Regex("Bearer\\s+[A-Za-z0-9._\\-~+/]+=*", RegexOption.IGNORE_CASE), "Bearer [REDACTED]")
        return out
    }

    private fun hostLabelOf(url: String): String {
        val afterScheme = url.trim().substringAfter("://")
        return afterScheme.substringBefore('/').substringBefore('?').ifBlank { url.trim() }
    }

    private fun cleanupAll(paths: List<String>) {
        paths.asReversed().forEach { raw ->
            runCatching {
                val p = raw.toPath()
                if (FileSystem.SYSTEM.exists(p)) {
                    FileSystem.SYSTEM.deleteRecursively(p)
                }
            }
        }
    }
}
