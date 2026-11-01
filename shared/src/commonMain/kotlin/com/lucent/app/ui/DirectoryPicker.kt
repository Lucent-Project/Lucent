package com.lucent.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucent.app.harness.HarnessRuntime
import com.lucent.app.i18n.S
import com.lucent.app.platform.LocalPlatformContext
import com.lucent.app.platform.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath

data class DirectoryEntry(val name: String, val path: String)

enum class UnreadableKind {
    NOT_FOUND,
    NOT_A_DIRECTORY,
    RESTRICTED_INTERMEDIATE,
    PERMISSION_REQUIRED,
    ACCESS_DENIED
}

sealed class DirectoryListOutcome {
    data class Listed(val entries: List<DirectoryEntry>) : DirectoryListOutcome()
    data class Unreadable(val kind: UnreadableKind, val detail: String = "") : DirectoryListOutcome()
}

sealed class SafeParentTarget {
    data class Folder(val path: String) : SafeParentTarget()
    data object RootsOverview : SafeParentTarget()
}

object DirectoryBrowse {

    fun isWindowsDrivePrefix(path: String): Boolean {
        val s = path.trim()
        return s.length >= 2 && s[0].isLetter() && s[1] == ':' &&
            (s.length == 2 || s[2] == '\\' || s[2] == '/')
    }

    fun isWindowsDriveRoot(path: String): Boolean {
        val s = path.trim()
        return s.length >= 2 && s[0].isLetter() && s[1] == ':' &&
            s.substring(2).all { it == '\\' || it == '/' }
    }

    fun normalize(path: String): String {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) return ""
        if (isWindowsDrivePrefix(trimmed)) {
            val drive = trimmed[0].uppercaseChar()
            val rest = trimmed.substring(2).replace('/', '\\').trim('\\')
            return if (rest.isEmpty()) "$drive:\\" else "$drive:\\$rest"
        }
        if (trimmed.startsWith("\\\\")) {
            val parts = trimmed.replace('/', '\\').trim('\\').split('\\').filter { it.isNotEmpty() }
            return if (parts.isEmpty()) "\\\\" else "\\\\" + parts.joinToString("\\")
        }
        val cut = trimmed.trimEnd('/', '\\')
        return cut.ifEmpty { "/" }
    }

    fun samePath(a: String, b: String): Boolean {
        val na = normalize(a)
        val nb = normalize(b)
        if (isWindowsDrivePrefix(na) || isWindowsDrivePrefix(nb) || na.startsWith("\\\\") || nb.startsWith("\\\\")) {
            return na.equals(nb, ignoreCase = true)
        }
        return na == nb
    }

    fun isPathInsideOrEqual(path: String, root: String): Boolean {
        val normPath = normalize(path)
        val normRoot = normalize(root)
        if (normPath.isEmpty() || normRoot.isEmpty()) return false
        if (samePath(normPath, normRoot)) return true
        if (isWindowsDrivePrefix(normRoot) || normRoot.startsWith("\\\\")) {
            val prefix = if (normRoot.endsWith("\\")) normRoot else "$normRoot\\"
            return normPath.startsWith(prefix, ignoreCase = true)
        }
        if (normRoot == "/") return normPath.startsWith("/")
        return normPath.startsWith("$normRoot/")
    }

    fun parent(path: String): String? {
        val clean = normalize(path)
        if (clean.isEmpty() || clean == "/") return null
        if (isWindowsDrivePrefix(clean)) {
            if (clean.length <= 3) return null
            val idx = clean.lastIndexOf('\\')
            return if (idx <= 2) "${clean[0]}:\\" else clean.substring(0, idx)
        }
        if (clean.startsWith("\\\\")) {
            val parts = clean.removePrefix("\\\\").split('\\').filter { it.isNotEmpty() }
            if (parts.size <= 2) return null
            return "\\\\" + parts.dropLast(1).joinToString("\\")
        }
        val p = clean.toPath()
        val up = p.parent ?: return null
        if (up.toString() == p.toString()) return null
        return normalize(up.toString())
    }

    fun resolve(path: String, base: String): String {
        val raw = path.trim()
        val normBase = normalize(base)
        if (raw.isEmpty() || raw == ".") return normBase
        if (raw == "..") return parent(normBase) ?: normBase
        if (isWindowsDrivePrefix(raw) || raw.startsWith("\\\\")) {
            val norm = normalize(raw)
            val walked = runCatching { FileSystem.SYSTEM.canonicalize(norm.toPath()).toString() }.getOrNull()
            return if (walked != null && (isWindowsDrivePrefix(walked) || walked.startsWith("\\\\"))) {
                normalize(walked)
            } else {
                norm
            }
        }
        if (isWindowsDrivePrefix(normBase) || normBase.startsWith("\\\\")) {
            if (raw.startsWith("/")) return normalize(raw)
            var cursor = normBase
            raw.replace('/', '\\').split('\\').filter { it.isNotEmpty() }.forEach { seg ->
                cursor = when (seg) {
                    "." -> cursor
                    ".." -> parent(cursor) ?: cursor
                    else -> child(cursor, seg)
                }
            }
            val walked = runCatching { FileSystem.SYSTEM.canonicalize(cursor.toPath()).toString() }.getOrNull()
            return if (walked != null && (isWindowsDrivePrefix(walked) || walked.startsWith("\\\\"))) {
                normalize(walked)
            } else {
                normalize(cursor)
            }
        }
        val clean = normalize(raw)
        val p = clean.toPath()
        val absolute = if (p.isAbsolute) p else normBase.toPath() / clean
        val walked = runCatching { FileSystem.SYSTEM.canonicalize(absolute).toString() }.getOrNull()
        return normalize(walked ?: absolute.toString())
    }

    private fun leafName(cleanPath: String): String {
        if (cleanPath == "/") return "/"
        if (isWindowsDriveRoot(cleanPath)) return cleanPath
        if (cleanPath.startsWith("\\\\")) {
            val parts = cleanPath.removePrefix("\\\\").split('\\').filter { it.isNotEmpty() }
            if (parts.size <= 2) return cleanPath
            return parts.last()
        }
        val sub = cleanPath.trimEnd('/', '\\')
        val slashIdx = maxOf(sub.lastIndexOf('/'), sub.lastIndexOf('\\'))
        return if (slashIdx >= 0 && slashIdx < sub.lastIndex) sub.substring(slashIdx + 1) else cleanPath
    }

    fun ancestors(path: String): List<DirectoryEntry> {
        val clean = normalize(path)
        if (clean.isEmpty()) return emptyList()
        val chain = mutableListOf<DirectoryEntry>()
        var cursor: String? = clean
        var guard = 0
        while (cursor != null && guard++ < 64) {
            chain.add(0, DirectoryEntry(leafName(cursor), cursor))
            cursor = parent(cursor)
        }
        return chain
    }

    fun ancestorsForRoots(path: String, roots: List<StorageRoot>): List<DirectoryEntry> {
        val clean = normalize(path)
        if (clean.isEmpty()) return emptyList()
        val matchingRoots = roots.filter { isPathInsideOrEqual(clean, it.path) }
        val anchor = matchingRoots
            .sortedByDescending { normalize(it.path).length }
            .firstOrNull { isListable(it.path) }
            ?: matchingRoots.maxByOrNull { normalize(it.path).length }
            ?: return ancestors(clean)

        val chain = mutableListOf<DirectoryEntry>()
        var cursor: String? = clean
        var guard = 0
        while (cursor != null && guard++ < 64) {
            if (samePath(cursor, anchor.path)) {
                chain.add(0, DirectoryEntry(anchor.name, normalize(anchor.path)))
                break
            }
            chain.add(0, DirectoryEntry(leafName(cursor), cursor))
            cursor = parent(cursor)
        }
        return chain
    }

    fun isRoot(path: String): Boolean = parent(path) == null

    fun child(path: String, name: String): String {
        val clean = normalize(path)
        val part = name.trim().trim('/', '\\')
        if (part.isEmpty()) return clean
        if (isWindowsDrivePrefix(clean) || clean.startsWith("\\\\")) {
            return if (clean.endsWith("\\")) "$clean$part" else "$clean\\$part"
        }
        return (clean.toPath() / part).toString()
    }

    fun hidden(name: String): Boolean = name.startsWith(".")

    fun isAndroidRestrictedIntermediate(path: String): Boolean {
        val clean = normalize(path)
        return clean == "/" ||
            clean == "/storage" ||
            clean == "/storage/emulated" ||
            clean == "/mnt" ||
            clean == "/storage/self" ||
            clean == "/storage/emulated/0/Android/data" ||
            clean == "/storage/emulated/0/Android/obb" ||
            clean == "/sdcard/Android/data" ||
            clean == "/sdcard/Android/obb"
    }

    private fun isLikelyAndroidSharedStorage(path: String): Boolean {
        val clean = normalize(path)
        if (clean.contains("/Android/data/") || clean.contains("/Android/media/")) return false
        return clean == "/storage/emulated/0" ||
            clean.startsWith("/storage/emulated/0/") ||
            clean == "/sdcard" ||
            clean.startsWith("/sdcard/")
    }

    fun listOutcome(
        path: String,
        showHidden: Boolean,
        android: Boolean = HarnessRuntime.android,
        storageGranted: Boolean = true
    ): DirectoryListOutcome {
        val clean = normalize(path)
        if (clean.isEmpty()) {
            return DirectoryListOutcome.Unreadable(UnreadableKind.NOT_FOUND)
        }
        if (android && isAndroidRestrictedIntermediate(clean)) {
            return DirectoryListOutcome.Unreadable(UnreadableKind.RESTRICTED_INTERMEDIATE)
        }
        val dir = clean.toPath()
        val listAttempt = runCatching { FileSystem.SYSTEM.list(dir) }
        val children = listAttempt.getOrNull()
        if (children != null) {
            val folders = children
                .asSequence()
                .filter {
                    runCatching { FileSystem.SYSTEM.metadataOrNull(it)?.isDirectory == true }.getOrDefault(false) &&
                        (showHidden || !hidden(it.name))
                }
                .map { DirectoryEntry(it.name, normalize(it.toString())) }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                .toList()
            return DirectoryListOutcome.Listed(folders)
        }
        val errMsg = listAttempt.exceptionOrNull()?.message.orEmpty()
        val meta = runCatching { FileSystem.SYSTEM.metadataOrNull(dir) }.getOrNull()
        if (meta != null && !meta.isDirectory) {
            return DirectoryListOutcome.Unreadable(UnreadableKind.NOT_A_DIRECTORY, errMsg)
        }
        if (android && !storageGranted && isLikelyAndroidSharedStorage(clean)) {
            return DirectoryListOutcome.Unreadable(UnreadableKind.PERMISSION_REQUIRED, errMsg)
        }
        val exists = meta != null || runCatching { FileSystem.SYSTEM.exists(dir) }.getOrDefault(false)
        if (!exists) {
            return DirectoryListOutcome.Unreadable(UnreadableKind.NOT_FOUND, errMsg)
        }
        val kind = if (android && !storageGranted) {
            UnreadableKind.PERMISSION_REQUIRED
        } else {
            UnreadableKind.ACCESS_DENIED
        }
        return DirectoryListOutcome.Unreadable(kind, errMsg)
    }

    fun list(path: String, showHidden: Boolean): List<DirectoryEntry>? {
        return when (val out = listOutcome(path, showHidden)) {
            is DirectoryListOutcome.Listed -> out.entries
            is DirectoryListOutcome.Unreadable -> null
        }
    }

    fun isListable(
        path: String,
        android: Boolean = HarnessRuntime.android,
        storageGranted: Boolean = true
    ): Boolean {
        val clean = normalize(path)
        if (clean.isEmpty()) return false
        return listOutcome(clean, showHidden = false, android = android, storageGranted = storageGranted) is DirectoryListOutcome.Listed
    }

    fun create(parent: String, name: String): String? {
        if (name.isBlank()) return null
        val targetPath = child(parent, name.trim()).toPath()
        if (FileSystem.SYSTEM.exists(targetPath)) return normalize(targetPath.toString())
        return try {
            FileSystem.SYSTEM.createDirectories(targetPath)
            normalize(targetPath.toString())
        } catch (_: Exception) {
            null
        }
    }

    fun storageRoots(context: PlatformContext? = null): List<StorageRoot> {
        val base = platformStorageRoots(context).toMutableList()
        if (HarnessRuntime.android) {
            val fallbackFiles = runCatching { normalize(HarnessRuntime.filesDirPath()) }.getOrDefault("")
            if (fallbackFiles.isNotEmpty() && base.none { samePath(it.path, fallbackFiles) } && isListable(fallbackFiles)) {
                base.add(
                    StorageRoot(
                        name = S.agentWorkspaceRootAppFiles,
                        path = fallbackFiles,
                        kind = StorageRootKind.APP_PRIVATE,
                        requiresPermission = false
                    )
                )
            }
        }
        return base
    }

    fun roots(context: PlatformContext? = null): List<DirectoryEntry> =
        storageRoots(context).map { DirectoryEntry(it.name, it.path) }

    fun home(): String = if (HarnessRuntime.android) {
        "/storage/emulated/0"
    } else {
        runCatching { HarnessRuntime.defaultWorkspacePath() }.getOrDefault("")
    }

    fun safeParent(
        path: String,
        roots: List<StorageRoot>,
        android: Boolean = HarnessRuntime.android,
        storageGranted: Boolean = true
    ): SafeParentTarget {
        val clean = normalize(path)
        if (clean.isEmpty()) return SafeParentTarget.RootsOverview
        if (roots.any { samePath(it.path, clean) }) {
            return SafeParentTarget.RootsOverview
        }
        var cursor = parent(clean)
        var guard = 0
        while (cursor != null && guard++ < 64) {
            if (isListable(cursor, android = android, storageGranted = storageGranted)) {
                return SafeParentTarget.Folder(cursor)
            }
            if (roots.any { samePath(it.path, cursor!!) }) {
                return SafeParentTarget.RootsOverview
            }
            cursor = parent(cursor)
        }
        return SafeParentTarget.RootsOverview
    }

    fun startingPoint(
        wanted: String,
        roots: List<StorageRoot>,
        android: Boolean = HarnessRuntime.android,
        storageGranted: Boolean = true
    ): String {
        var cursor: String? = normalize(wanted)
        var guard = 0
        while (!cursor.isNullOrEmpty() && guard++ < 64) {
            if (isListable(cursor, android = android, storageGranted = storageGranted)) {
                return cursor
            }
            cursor = parent(cursor)
        }
        val homeDir = normalize(home())
        if (homeDir.isNotEmpty() && isListable(homeDir, android = android, storageGranted = storageGranted)) {
            return homeDir
        }
        val listableRoot = roots.firstOrNull {
            isListable(it.path, android = android, storageGranted = storageGranted)
        }
        if (listableRoot != null) return normalize(listableRoot.path)
        var homeParent = parent(homeDir)
        var homeGuard = 0
        while (!homeParent.isNullOrEmpty() && homeGuard++ < 32) {
            if (isListable(homeParent, android = android, storageGranted = storageGranted)) {
                return homeParent
            }
            homeParent = parent(homeParent)
        }
        return roots.firstOrNull()?.path?.let(::normalize)
            ?: homeDir.ifEmpty { okio.Path.DIRECTORY_SEPARATOR }
    }

    fun startingPoint(wanted: String): String =
        startingPoint(wanted, storageRoots(null))
}

@Composable
fun DirectoryPickerDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit
) {
    val context = LocalPlatformContext.current
    val initialNormalized = remember(initialPath) { DirectoryBrowse.normalize(initialPath) }
    var current by remember {
        mutableStateOf(
            DirectoryBrowse.startingPoint(
                wanted = initialPath,
                roots = DirectoryBrowse.storageRoots(context),
                storageGranted = platformStorageAccessGranted(context)
            )
        )
    }
    val storageAccess = rememberPlatformStorageAccess(
        currentPath = current,
        onSystemPicked = { picked ->
            val clean = DirectoryBrowse.normalize(picked)
            if (clean.isNotEmpty()) {
                onOpen(clean)
            }
        }
    )
    val roots = remember(context, storageAccess.granted) {
        DirectoryBrowse.storageRoots(context)
    }
    var inRootsOverview by remember {
        mutableStateOf(
            initialNormalized.isEmpty() ||
                !DirectoryBrowse.isListable(
                    initialNormalized,
                    storageGranted = storageAccess.granted
                )
        )
    }
    var showHidden by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var outcome by remember {
        mutableStateOf<DirectoryListOutcome>(DirectoryListOutcome.Listed(emptyList()))
    }
    var creating by remember { mutableStateOf(false) }

    LaunchedEffect(current, showHidden, inRootsOverview, storageAccess.granted) {
        if (!inRootsOverview) {
            outcome = withContext(Dispatchers.IO) {
                DirectoryBrowse.listOutcome(
                    path = current,
                    showHidden = showHidden,
                    storageGranted = storageAccess.granted
                )
            }
        }
    }

    fun openFolder(path: String) {
        val resolved = if (inRootsOverview) {
            DirectoryBrowse.normalize(path)
        } else {
            DirectoryBrowse.resolve(path, current)
        }
        if (resolved.isNotEmpty()) {
            current = resolved
            inRootsOverview = false
            editing = false
        }
    }

    fun goUp() {
        when (
            val target = DirectoryBrowse.safeParent(
                path = current,
                roots = roots,
                storageGranted = storageAccess.granted
            )
        ) {
            is SafeParentTarget.Folder -> {
                current = target.path
                inRootsOverview = false
            }
            SafeParentTarget.RootsOverview -> {
                inRootsOverview = true
            }
        }
        editing = false
    }

    val currentSelectable = !inRootsOverview && outcome is DirectoryListOutcome.Listed

    LucentGlassDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(S.agentWorkspacePickTitle, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(6.dp))
                PathRow(
                    path = current,
                    roots = roots,
                    inRootsOverview = inRootsOverview,
                    editing = editing,
                    draft = draft,
                    onDraftChange = { draft = it },
                    onToggleEditing = {
                        draft = if (inRootsOverview) "" else current
                        editing = !editing
                    },
                    onShowRoots = {
                        inRootsOverview = true
                        editing = false
                    },
                    onGo = { openFolder(it) },
                    onSubmit = {
                        if (draft.isNotBlank()) {
                            openFolder(draft)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                ShortcutRow(
                    current = current,
                    inRootsOverview = inRootsOverview,
                    roots = roots,
                    onShowRoots = {
                        inRootsOverview = true
                        editing = false
                    },
                    onGo = { openFolder(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (inRootsOverview) {
                    StorageRootsList(
                        roots = roots,
                        storageAccess = storageAccess,
                        onBrowseRoot = { root ->
                            current = DirectoryBrowse.normalize(root.path)
                            inRootsOverview = false
                            editing = false
                        },
                        onSelectRoot = { root ->
                            onOpen(DirectoryBrowse.normalize(root.path))
                        }
                    )
                } else {
                    DirectoryList(
                        outcome = outcome,
                        storageAccess = storageAccess,
                        onUp = { goUp() },
                        onShowRoots = {
                            inRootsOverview = true
                            editing = false
                        },
                        onGo = { openFolder(it) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                PickerActions(
                    inRootsOverview = inRootsOverview,
                    canSelectCurrent = currentSelectable,
                    showHidden = showHidden,
                    onToggleHidden = { showHidden = !showHidden },
                    onNewFolder = { creating = true },
                    onSystemPicker = storageAccess.openSystemPicker,
                    onDismiss = onDismiss,
                    onOpen = { if (currentSelectable) onOpen(current) }
                )
            }
        }
    }

    if (creating && currentSelectable) {
        NewFolderDialog(
            parent = current,
            onDismiss = { creating = false },
            onCreate = { name ->
                DirectoryBrowse.create(current, name)?.let { openFolder(it) }
                creating = false
            }
        )
    }
}

@Composable
private fun PickerActions(
    inRootsOverview: Boolean,
    canSelectCurrent: Boolean,
    showHidden: Boolean,
    onToggleHidden: () -> Unit,
    onNewFolder: () -> Unit,
    onSystemPicker: (() -> Unit)?,
    onDismiss: () -> Unit,
    onOpen: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!inRootsOverview && canSelectCurrent) {
                TextButton(onClick = onNewFolder) {
                    Text(S.agentWorkspaceNewFolder, fontSize = 13.sp)
                }
            }
            if (!inRootsOverview) {
                TextButton(onClick = onToggleHidden) {
                    Text(
                        S.agentWorkspaceShowHidden,
                        fontSize = 13.sp,
                        color = if (showHidden) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onSystemPicker != null) {
                TextButton(onClick = onSystemPicker) {
                    Text(S.agentWorkspaceSystemPicker, fontSize = 13.sp)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) { Text(S.actionCancel, fontSize = 13.sp) }
            if (!inRootsOverview) {
                TextButton(
                    enabled = canSelectCurrent,
                    onClick = onOpen
                ) {
                    Text(S.agentWorkspaceSelectFolder, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun NewFolderDialog(parent: String, onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    LucentGlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(S.agentWorkspaceNewFolderTitle) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = com.lucent.app.collapseExcessBlankLines(it) },
                label = { Text(S.agentWorkspaceLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name) }
            ) { Text(S.actionCreate) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(S.actionCancel) } }
    )
    if (parent.isBlank()) return
}

@Composable
private fun PathRow(
    path: String,
    roots: List<StorageRoot>,
    inRootsOverview: Boolean,
    editing: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onToggleEditing: () -> Unit,
    onShowRoots: () -> Unit,
    onGo: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (editing) {
            OutlinedTextField(
                value = draft,
                onValueChange = { onDraftChange(com.lucent.app.collapseExcessBlankLines(it)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onSubmit) { Text(S.actionOpen, fontSize = 13.sp) }
        } else {
            Breadcrumbs(
                path = path,
                roots = roots,
                inRootsOverview = inRootsOverview,
                onShowRoots = onShowRoots,
                onGo = onGo,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleEditing) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = S.agentWorkspaceEditPath,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Breadcrumbs(
    path: String,
    roots: List<StorageRoot>,
    inRootsOverview: Boolean,
    onShowRoots: () -> Unit,
    onGo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val crumbs = remember(path, roots, inRootsOverview) {
        if (inRootsOverview) emptyList() else DirectoryBrowse.ancestorsForRoots(path, roots)
    }
    val scroll = rememberScrollState()
    LaunchedEffect(crumbs, inRootsOverview) { scroll.scrollTo(scroll.maxValue) }
    Row(
        modifier = modifier.horizontalScroll(scroll),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            S.agentWorkspaceStorageRoots,
            fontSize = 13.sp,
            maxLines = 1,
            color = if (inRootsOverview) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(enabled = !inRootsOverview) { onShowRoots() }
                .padding(horizontal = 2.dp, vertical = 6.dp)
        )
        crumbs.forEachIndexed { index, crumb ->
            Text(
                "\u203A",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            val last = index == crumbs.lastIndex
            Text(
                crumb.name,
                fontSize = 13.sp,
                maxLines = 1,
                color = if (last) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(enabled = !last) { onGo(crumb.path) }
                    .padding(horizontal = 2.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ShortcutRow(
    current: String,
    inRootsOverview: Boolean,
    roots: List<StorageRoot>,
    onShowRoots: () -> Unit,
    onGo: (String) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onShowRoots) {
            Text(
                S.agentWorkspaceStorageRoots,
                fontSize = 12.sp,
                color = if (inRootsOverview) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        roots.forEach { root ->
            val active = !inRootsOverview && DirectoryBrowse.samePath(root.path, current)
            TextButton(onClick = { onGo(root.path) }) {
                Text(
                    root.name,
                    fontSize = 12.sp,
                    color = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StorageRootsList(
    roots: List<StorageRoot>,
    storageAccess: PlatformStorageAccess,
    onBrowseRoot: (StorageRoot) -> Unit,
    onSelectRoot: (StorageRoot) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            S.agentWorkspaceStorageRootsHint,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!storageAccess.granted && storageAccess.canRequestPermission) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = storageAccess.requestAccess) {
                Text(S.agentGrantStorage, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
            items(roots, key = { it.path }) { root ->
                val needsPerm = root.requiresPermission && !storageAccess.granted
                val listable = remember(root.path, storageAccess.granted) {
                    DirectoryBrowse.isListable(root.path, storageGranted = storageAccess.granted)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBrowseRoot(root) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (needsPerm && !listable) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(root.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val subtitle = if (needsPerm && !listable) {
                            "${root.path} \u00B7 ${S.agentWorkspaceNeedsPermissionBadge}"
                        } else {
                            root.path
                        }
                        Text(
                            subtitle,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (needsPerm && !listable) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    if (listable) {
                        TextButton(onClick = { onSelectRoot(root) }) {
                            Text(S.actionOpen, fontSize = 12.sp)
                        }
                    }
                    Text("\u203A", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DirectoryList(
    outcome: DirectoryListOutcome,
    storageAccess: PlatformStorageAccess,
    onUp: () -> Unit,
    onShowRoots: () -> Unit,
    onGo: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (outcome is DirectoryListOutcome.Unreadable) {
            val reason = when (outcome.kind) {
                UnreadableKind.PERMISSION_REQUIRED -> S.agentWorkspaceUnreadablePermission
                UnreadableKind.RESTRICTED_INTERMEDIATE -> S.agentWorkspaceUnreadableRestricted
                UnreadableKind.NOT_FOUND -> S.agentWorkspaceUnreadableNotFound
                UnreadableKind.NOT_A_DIRECTORY -> S.agentWorkspaceUnreadableNotDirectory
                UnreadableKind.ACCESS_DENIED -> S.agentWorkspaceUnreadableDenied
            }
            Text(
                S.agentWorkspaceUnreadable,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                reason,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (outcome.detail.isNotBlank() && outcome.kind == UnreadableKind.ACCESS_DENIED) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    outcome.detail,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (
                    (outcome.kind == UnreadableKind.PERMISSION_REQUIRED || !storageAccess.granted) &&
                    storageAccess.canRequestPermission
                ) {
                    TextButton(onClick = storageAccess.requestAccess) {
                        Text(S.agentGrantStorage, fontSize = 12.sp)
                    }
                }
                if (storageAccess.openSystemPicker != null) {
                    TextButton(onClick = storageAccess.openSystemPicker) {
                        Text(S.agentWorkspaceSystemPicker, fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onShowRoots) {
                    Text(S.agentWorkspaceJumpReadableRoot, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
            item {
                DirectoryRow(name = "..", detail = S.agentWorkspaceUp, onClick = onUp)
            }
            if (outcome is DirectoryListOutcome.Listed) {
                items(outcome.entries, key = { it.path }) { entry ->
                    DirectoryRow(name = entry.name, detail = null, onClick = { onGo(entry.path) })
                }
                if (outcome.entries.isEmpty()) {
                    item {
                        Text(
                            S.agentWorkspaceEmpty,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectoryRow(name: String, detail: String?, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (detail != null) {
                Text(detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("\u203A", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
