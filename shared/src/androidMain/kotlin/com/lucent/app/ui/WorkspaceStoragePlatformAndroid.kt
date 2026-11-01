package com.lucent.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lucent.app.i18n.S
import com.lucent.app.platform.PlatformContext
import java.io.File

actual fun platformStorageAccessGranted(context: PlatformContext?): Boolean {
    return if (Build.VERSION.SDK_INT >= 30) {
        runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)
    } else {
        if (context == null) return true
        val readOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val writeOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        readOk && writeOk
    }
}

fun requestAndroidAllFilesAccess(context: Context) {
    val pkgUri = Uri.parse("package:${context.packageName}")
    if (Build.VERSION.SDK_INT >= 30) {
        val perApp = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, pkgUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launched = runCatching { context.startActivity(perApp) }.isSuccess
        if (!launched) {
            val allApps = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(allApps) }
        }
    } else {
        val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkgUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(appDetails) }
    }
}

private fun isDirReadable(file: File?): Boolean {
    if (file == null) return false
    return runCatching { file.isDirectory && file.list() != null }.getOrDefault(false)
}

actual fun platformStorageRoots(context: PlatformContext?): List<StorageRoot> {
    val roots = mutableListOf<StorageRoot>()
    val seen = LinkedHashSet<String>()

    fun addRoot(root: StorageRoot) {
        val key = DirectoryBrowse.normalize(root.path)
        if (key.isNotEmpty() && seen.add(key)) {
            roots.add(root.copy(path = key))
        }
    }

    val primaryShared = runCatching {
        Environment.getExternalStorageDirectory().absolutePath
    }.getOrDefault("/storage/emulated/0").ifBlank { "/storage/emulated/0" }

    addRoot(
        StorageRoot(
            name = S.agentWorkspaceRootShared,
            path = primaryShared,
            kind = StorageRootKind.SHARED,
            requiresPermission = true
        )
    )

    if (context != null) {
        val internalFiles = runCatching { context.filesDir }.getOrNull()
        if (internalFiles != null) {
            runCatching { internalFiles.mkdirs() }
            if (isDirReadable(internalFiles)) {
                addRoot(
                    StorageRoot(
                        name = S.agentWorkspaceRootAppFiles,
                        path = internalFiles.absolutePath,
                        kind = StorageRootKind.APP_PRIVATE,
                        requiresPermission = false
                    )
                )
            }
        }

        val externalAppDirs = runCatching { context.getExternalFilesDirs(null).filterNotNull() }.getOrDefault(emptyList())
        val primaryAppExt = externalAppDirs.firstOrNull()
        if (primaryAppExt != null) {
            runCatching { primaryAppExt.mkdirs() }
            if (isDirReadable(primaryAppExt)) {
                addRoot(
                    StorageRoot(
                        name = S.agentWorkspaceRootAppExternal,
                        path = primaryAppExt.absolutePath,
                        kind = StorageRootKind.APP_PRIVATE,
                        requiresPermission = false
                    )
                )
            }
        }

        if (Build.VERSION.SDK_INT >= 30) {
            val sm = runCatching {
                context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            }.getOrNull()
            sm?.storageVolumes.orEmpty().forEach { vol ->
                if (!vol.isPrimary) {
                    val dir = runCatching { vol.directory }.getOrNull()
                    if (isDirReadable(dir)) {
                        val label = runCatching { vol.getDescription(context) }.getOrNull()
                            .orEmpty()
                            .ifBlank { dir!!.name }
                        addRoot(
                            StorageRoot(
                                name = S.agentWorkspaceRootExternalVolume(label),
                                path = dir!!.absolutePath,
                                kind = StorageRootKind.EXTERNAL_VOLUME,
                                requiresPermission = false
                            )
                        )
                    }
                }
            }
        }

        externalAppDirs.drop(1).forEach { extDir ->
            runCatching { extDir.mkdirs() }
            val rawPath = extDir.absolutePath
            val markerIdx = rawPath.indexOf("/Android/data/")
            val volumeRoot = if (markerIdx > 0) File(rawPath.substring(0, markerIdx)) else null
            if (isDirReadable(volumeRoot)) {
                addRoot(
                    StorageRoot(
                        name = S.agentWorkspaceRootExternalVolume(volumeRoot!!.name),
                        path = volumeRoot.absolutePath,
                        kind = StorageRootKind.EXTERNAL_VOLUME,
                        requiresPermission = false
                    )
                )
            } else if (isDirReadable(extDir)) {
                val volLabel = volumeRoot?.name?.ifBlank { null } ?: extDir.name
                addRoot(
                    StorageRoot(
                        name = S.agentWorkspaceRootExternalVolume(volLabel),
                        path = rawPath,
                        kind = StorageRootKind.APP_PRIVATE,
                        requiresPermission = false
                    )
                )
            }
        }
    }

    return roots
}

private fun resolveDocumentTreeUri(context: Context, treeUri: Uri): String? {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
    val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
    if (docId.startsWith("raw:")) {
        return docId.removePrefix("raw:").takeIf { it.isNotBlank() }
    }
    val primaryBase = runCatching {
        Environment.getExternalStorageDirectory().absolutePath
    }.getOrDefault("/storage/emulated/0").trimEnd('/')
    if (docId.startsWith("primary:")) {
        val rel = docId.removePrefix("primary:").trim('/')
        return if (rel.isEmpty()) primaryBase else "$primaryBase/$rel"
    }
    if (docId.startsWith("home:")) {
        val docsBase = runCatching {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
        }.getOrDefault("$primaryBase/Documents").trimEnd('/')
        val rel = docId.removePrefix("home:").trim('/')
        return if (rel.isEmpty()) docsBase else "$docsBase/$rel"
    }
    if (docId.contains(":")) {
        val uuid = docId.substringBefore(":").trim()
        val rel = docId.substringAfter(":").trim('/')
        var volumeBase: String? = null
        if (Build.VERSION.SDK_INT >= 30) {
            val sm = runCatching {
                context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            }.getOrNull()
            volumeBase = sm?.storageVolumes.orEmpty()
                .firstOrNull { it.uuid != null && it.uuid.equals(uuid, ignoreCase = true) }
                ?.directory
                ?.absolutePath
        }
        if (volumeBase.isNullOrBlank()) {
            val candidate = File("/storage/$uuid")
            if (candidate.exists()) volumeBase = candidate.absolutePath
        }
        if (!volumeBase.isNullOrBlank()) {
            val cleanBase = volumeBase.trimEnd('/')
            return if (rel.isEmpty()) cleanBase else "$cleanBase/$rel"
        }
    }
    return null
}

@Composable
actual fun rememberPlatformStorageAccess(
    currentPath: String,
    onSystemPicked: (String) -> Unit
): PlatformStorageAccess {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnPicked by rememberUpdatedState(onSystemPicked)
    var granted by remember(context) { mutableStateOf(platformStorageAccessGranted(context)) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = platformStorageAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val legacyPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val nowGranted = platformStorageAccessGranted(context)
        granted = nowGranted
        if (!nowGranted) {
            requestAndroidAllFilesAccess(context)
        }
    }

    val treeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val resolved = resolveDocumentTreeUri(context, uri)
            if (!resolved.isNullOrBlank()) {
                latestOnPicked(DirectoryBrowse.normalize(resolved))
            }
        }
    }

    val requestAccess: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 30) {
            requestAndroidAllFilesAccess(context)
        } else {
            legacyPermLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    val openSystemPicker: () -> Unit = {
        runCatching { treeLauncher.launch(null) }
    }

    return PlatformStorageAccess(
        granted = granted,
        canRequestPermission = true,
        requestAccess = requestAccess,
        openSystemPicker = openSystemPicker
    )
}
