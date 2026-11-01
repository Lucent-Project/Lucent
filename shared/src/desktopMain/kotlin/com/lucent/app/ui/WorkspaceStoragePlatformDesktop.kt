package com.lucent.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.lucent.app.i18n.S
import com.lucent.app.platform.PlatformContext
import com.lucent.desktop.platform.DesktopFiles
import java.io.File

private fun isDesktopWindows(): Boolean =
    System.getProperty("os.name").orEmpty().lowercase().contains("win") || File.separatorChar == '\\'

private fun isReadableDirectory(file: File?): Boolean {
    if (file == null) return false
    return runCatching { file.isDirectory && file.list() != null }.getOrDefault(false)
}

actual fun platformStorageAccessGranted(context: PlatformContext?): Boolean = true

actual fun platformStorageRoots(context: PlatformContext?): List<StorageRoot> {
    val roots = mutableListOf<StorageRoot>()
    val seen = LinkedHashSet<String>()
    val windows = isDesktopWindows()

    fun addRoot(root: StorageRoot) {
        val norm = DirectoryBrowse.normalize(root.path)
        val dedupKey = if (windows) norm.lowercase() else norm
        if (norm.isNotEmpty() && seen.add(dedupKey)) {
            roots.add(root.copy(path = norm))
        }
    }

    val homeProp = System.getProperty("user.home").orEmpty().trim()
    if (homeProp.isNotEmpty()) {
        val homeDir = File(homeProp)
        if (isReadableDirectory(homeDir)) {
            addRoot(
                StorageRoot(
                    name = S.agentWorkspaceHome,
                    path = homeDir.absolutePath,
                    kind = StorageRootKind.USER_HOME
                )
            )
            val docsDir = File(homeDir, "Documents")
            if (isReadableDirectory(docsDir)) {
                addRoot(
                    StorageRoot(
                        name = S.agentWorkspaceRootDocuments,
                        path = docsDir.absolutePath,
                        kind = StorageRootKind.USER_HOME
                    )
                )
            }
        }
    }

    if (windows) {
        val driveRoots = runCatching { File.listRoots()?.toList().orEmpty() }.getOrDefault(emptyList())
            .sortedBy { it.path.uppercase() }
        driveRoots.forEach { drive ->
            val raw = drive.path.trim()
            if (raw.isNotEmpty() && runCatching { drive.exists() }.getOrDefault(false)) {
                val label = raw.trimEnd('\\', '/')
                addRoot(
                    StorageRoot(
                        name = S.agentWorkspaceRootDrive(label),
                        path = DirectoryBrowse.normalize(raw),
                        kind = StorageRootKind.DRIVE
                    )
                )
            }
        }
    } else {
        listOf("/Volumes", "/media", "/mnt").forEach { mountParentPath ->
            val mountParent = File(mountParentPath)
            val children = runCatching { mountParent.listFiles()?.toList().orEmpty() }.getOrDefault(emptyList())
                .sortedBy { it.name.lowercase() }
            children.forEach { child ->
                if (isReadableDirectory(child)) {
                    addRoot(
                        StorageRoot(
                            name = S.agentWorkspaceRootExternalVolume(child.name),
                            path = child.absolutePath,
                            kind = StorageRootKind.EXTERNAL_VOLUME
                        )
                    )
                }
            }
        }
        val sysRoot = File("/")
        if (isReadableDirectory(sysRoot)) {
            addRoot(
                StorageRoot(
                    name = S.agentWorkspaceRootSystem,
                    path = "/",
                    kind = StorageRootKind.SYSTEM_ROOT
                )
            )
        }
    }

    return roots
}

@Composable
actual fun rememberPlatformStorageAccess(
    currentPath: String,
    onSystemPicked: (String) -> Unit
): PlatformStorageAccess {
    val latestPath by rememberUpdatedState(currentPath)
    val latestOnPicked by rememberUpdatedState(onSystemPicked)
    val openPicker: () -> Unit = {
        val startFile = latestPath.takeIf { it.isNotBlank() }?.let(::File)
        val picked = DesktopFiles.chooseFolder(
            title = S.agentWorkspacePickTitle,
            startIn = startFile
        )
        if (picked != null) {
            latestOnPicked(DirectoryBrowse.normalize(picked.absolutePath))
        }
    }
    return PlatformStorageAccess(
        granted = true,
        canRequestPermission = false,
        requestAccess = {},
        openSystemPicker = openPicker
    )
}
