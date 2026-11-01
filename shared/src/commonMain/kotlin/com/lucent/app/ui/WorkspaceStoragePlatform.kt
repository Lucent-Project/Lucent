package com.lucent.app.ui

import androidx.compose.runtime.Composable
import com.lucent.app.platform.PlatformContext

enum class StorageRootKind {
    SHARED,
    APP_PRIVATE,
    EXTERNAL_VOLUME,
    DRIVE,
    USER_HOME,
    SYSTEM_ROOT
}

data class StorageRoot(
    val name: String,
    val path: String,
    val kind: StorageRootKind,
    val requiresPermission: Boolean = false
)

data class PlatformStorageAccess(
    val granted: Boolean,
    val canRequestPermission: Boolean,
    val requestAccess: () -> Unit,
    val openSystemPicker: (() -> Unit)?
)

expect fun platformStorageRoots(context: PlatformContext?): List<StorageRoot>

expect fun platformStorageAccessGranted(context: PlatformContext?): Boolean

@Composable
expect fun rememberPlatformStorageAccess(
    currentPath: String,
    onSystemPicked: (String) -> Unit
): PlatformStorageAccess
