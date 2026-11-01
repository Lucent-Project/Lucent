package com.lucent.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucent.app.harness.HarnessRuntime
import com.lucent.app.harness.PluginState
import com.lucent.app.harness.plugins.AptDiagnostics
import com.lucent.app.harness.plugins.InstallDiagnostic
import com.lucent.app.harness.plugins.InstallFailureKind
import com.lucent.app.harness.plugins.PluginCatalog
import com.lucent.app.harness.plugins.PluginSetup
import com.lucent.app.harness.plugins.PluginSource
import com.lucent.app.harness.plugins.PluginSpec
import com.lucent.app.harness.plugins.SetupStep
import com.lucent.app.harness.plugins.SetupStepState
import com.lucent.app.i18n.S
import com.lucent.app.ui.BackHeader
import com.lucent.app.ui.DirectoryPickerDialog
import com.lucent.app.ui.LocalOnGradient
import com.lucent.app.ui.LocalOnGradientMuted
import com.lucent.app.ui.frostedGlass
import kotlinx.coroutines.launch

@Composable
fun PluginSetupWizard(onBack: () -> Unit) {
    val android = HarnessRuntime.android
    val scope = rememberCoroutineScope()
    var steps by remember { mutableStateOf<List<SetupStep>>(emptyList()) }
    var busy by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var diagnostic by remember { mutableStateOf<InstallDiagnostic?>(null) }
    var logLines by remember { mutableStateOf(listOf<String>()) }
    var statusMsg by remember { mutableStateOf("") }
    var errorStep by remember { mutableStateOf("") }
    var setupComplete by remember { mutableStateOf(HarnessRuntime.config().setupComplete) }
    var mirrorRegion by remember { mutableStateOf(HarnessRuntime.config().pluginMirrorRegion) }
    var pickingWorkspace by remember { mutableStateOf(false) }
    var optionalPlugins by remember { mutableStateOf<List<PluginSpec>>(emptyList()) }

    val reload = {
        scope.launch {
            mirrorRegion = HarnessRuntime.config().pluginMirrorRegion
            steps = PluginSetup.computeSteps()
            val passes = PluginSetup.fullSetupPasses(steps)
            setupComplete = passes
            if (passes) {
                val cfg = HarnessRuntime.config()
                if (!cfg.setupComplete) {
                    HarnessRuntime.update(
                        cfg.copy(
                            setupComplete = true,
                            setupCompletedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun selectMirrorRegion(nextRegion: String) {
        mirrorRegion = nextRegion
        HarnessRuntime.update(HarnessRuntime.config().copy(pluginMirrorRegion = nextRegion))
    }

    fun installOptional(plugin: PluginSpec) {
        if (busy.isNotEmpty()) return
        val target = HarnessRuntime.pluginHost ?: return
        busy = "optional:" + plugin.id
        progress = 0f
        statusMsg = plugin.name
        logLines = emptyList()
        scope.launch {
            val outcome = target.install(
                plugin,
                PluginSource("", "", ""),
                onProgress = { p, t ->
                    progress = p
                    statusMsg = "${plugin.name}: $t"
                },
                onOutput = { line ->
                    logLines = (logLines + line).takeLast(300)
                }
            )
            if (!outcome.ok) {
                logLines = (logLines + "[install failed] " + outcome.message).takeLast(300)
            }
            statusMsg = ""
            busy = ""
            reload()
        }
    }

    LaunchedEffect(Unit) { reload() }
    LaunchedEffect(Unit) {
        optionalPlugins = PluginCatalog.forPlatformEffective(android)
            .filter { !it.required && it.id != "playwright" && it.id != "ubuntu" }
    }

    DisposableEffect(Unit) {
        val listener: (com.lucent.app.harness.HarnessConfig) -> Unit = { reload() }
        HarnessRuntime.observe(listener)
        onDispose { HarnessRuntime.unobserve(listener) }
    }

    val onGradient = LocalOnGradient.current
    val onGradientMuted = LocalOnGradientMuted.current

    Column(modifier = Modifier.fillMaxWidth()) {
        BackHeader(onBack = onBack)

        if (setupComplete) {
            Column(modifier = Modifier.fillMaxWidth().frostedGlass().padding(16.dp)) {
                Text(S.setupWizardDone, color = onGradient, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(S.setupWizardDoneBody, color = onGradientMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack) {
                    Text(S.setupActionDone, color = onGradient, fontSize = 13.sp)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth().frostedGlass().padding(16.dp)) {
                Text(S.setupWizardTitle, color = onGradient, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (android) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(S.pluginMirrorRegion, color = onGradientMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "auto" to S.pluginMirrorAuto,
                            "global" to S.pluginMirrorGlobal,
                            "cn" to S.pluginMirrorChina
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = mirrorRegion == key,
                                enabled = busy.isEmpty(),
                                onClick = { selectMirrorRegion(key) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            steps.forEach { step ->
                SetupStepCard(
                    step = step,
                    busy = busy == step.id,
                    progress = progress,
                    diagnostic = if (errorStep == step.id) diagnostic else null,
                    statusText = if (busy == step.id) statusMsg else "",
                    logLines = if (busy == step.id || errorStep == step.id) logLines else emptyList(),
                    onClearLog = { logLines = emptyList() },
                    onGradient = onGradient,
                    onGradientMuted = onGradientMuted,
                    onReload = { reload() },
                    onAction = {
                        diagnostic = null
                        errorStep = ""
                        statusMsg = ""
                        logLines = emptyList()
                        when (step.id) {
                            "workspace_shared" -> {
                                pickingWorkspace = true
                            }
                            "disk_space" -> {
                                reload()
                            }
                            "bundled_env" -> {
                                busy = step.id
                                scope.launch {
                                    val captured = mutableListOf<String>()
                                    val outcome = HarnessRuntime.host?.extractBuiltinRuntime { line ->
                                        statusMsg = line
                                        captured.add(line)
                                        logLines = (logLines + line).takeLast(300)
                                    }
                                    if (outcome?.ok == true) {
                                        reload()
                                    } else {
                                        diagnostic = AptDiagnostics.analyze(
                                            failure = null,
                                            message = outcome?.stderr.orEmpty(),
                                            detail = outcome?.text.orEmpty(),
                                            liveLog = captured
                                        )
                                        errorStep = step.id
                                    }
                                    statusMsg = ""
                                    busy = ""
                                }
                            }
                            "base_tools" -> {
                                busy = step.id
                                scope.launch {
                                    var failure: com.lucent.app.harness.PluginOutcome? = null
                                    val captured = mutableListOf<String>()
                                    val tools = PluginCatalog.forPlatformEffective(HarnessRuntime.android)
                                        .filter { it.id != "ubuntu" && it.id != "playwright" && it.required }
                                    for (tool in tools) {
                                        val alreadyDetected = HarnessRuntime.pluginHost?.detect(tool) == true
                                        if (alreadyDetected) {
                                            if (!HarnessRuntime.config().pluginInstalled(tool.id)) {
                                                HarnessRuntime.update(
                                                    HarnessRuntime.config().withPlugin(
                                                        PluginState(
                                                            id = tool.id,
                                                            installed = true,
                                                            source = "detected",
                                                            version = System.currentTimeMillis().toString(),
                                                            sizeBytes = tool.bytes,
                                                            installedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                )
                                            }
                                            continue
                                        }
                                        val outcome = HarnessRuntime.pluginHost?.install(
                                            tool,
                                            PluginSource("", "", ""),
                                            onProgress = { p, t ->
                                                progress = p
                                                statusMsg = "${tool.name}: $t"
                                            },
                                            onOutput = { line ->
                                                captured.add(line)
                                                logLines = (logLines + line).takeLast(300)
                                            }
                                        )
                                        if (outcome?.ok != true) {
                                            failure = outcome ?: com.lucent.app.harness.PluginOutcome(
                                                false,
                                                "${tool.name}: plugin host unavailable",
                                                failure = com.lucent.app.harness.PluginFailure.NO_SHELL
                                            )
                                            break
                                        }
                                    }
                                    if (failure != null) {
                                        diagnostic = AptDiagnostics.analyze(
                                            failure = failure.failure,
                                            message = failure.message,
                                            detail = failure.detail,
                                            liveLog = captured
                                        )
                                        errorStep = step.id
                                    }
                                    statusMsg = ""
                                    reload()
                                    busy = ""
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val baseToolsDone = steps.any { it.id == "base_tools" && it.passed }
            val pendingOptionals = optionalPlugins.filter { !HarnessRuntime.config().pluginInstalled(it.id) }
            if (baseToolsDone && pendingOptionals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth().frostedGlass().padding(16.dp)) {
                    Text(S.setupOptionalTitle, color = onGradient, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(S.setupOptionalBody, color = onGradientMuted, fontSize = 13.sp)
                }
                pendingOptionals.forEach { plugin ->
                    Spacer(modifier = Modifier.height(12.dp))
                    if (plugin.id == "adb") {
                        Text(S.pluginAdbWirelessHelp, color = onGradientMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    PluginRow(
                        plugin = plugin,
                        installed = false,
                        shellReady = HarnessRuntime.pluginHost?.isReady() == true,
                        busy = busy == "optional:" + plugin.id,
                        progress = progress,
                        note = if (busy == "optional:" + plugin.id) statusMsg else "",
                        logLines = if (busy == "optional:" + plugin.id) logLines else emptyList(),
                        onGradient = onGradient,
                        onGradientMuted = onGradientMuted,
                        onAction = { installOptional(plugin) },
                        onCancel = {
                            busy = ""
                            statusMsg = ""
                        },
                        onClearLog = { logLines = emptyList() }
                    )
                }
            }
        }
    }

    if (pickingWorkspace) {
        DirectoryPickerDialog(
            initialPath = HarnessRuntime.workspacePath(),
            onDismiss = { pickingWorkspace = false },
            onOpen = { picked ->
                pickingWorkspace = false
                HarnessRuntime.update(HarnessRuntime.config().copy(workspace = picked))
                scope.launch { reload() }
            }
        )
    }
}

@Composable
private fun SetupStepCard(
    step: SetupStep,
    busy: Boolean,
    progress: Float,
    diagnostic: InstallDiagnostic?,
    statusText: String,
    logLines: List<String>,
    onClearLog: () -> Unit,
    onGradient: androidx.compose.ui.graphics.Color,
    onGradientMuted: androidx.compose.ui.graphics.Color,
    onAction: () -> Unit,
    onReload: () -> Unit
) {
    val alpha = if (step.state == SetupStepState.WAITING) 0.5f else 1f
    Column(modifier = Modifier.fillMaxWidth().alpha(alpha).frostedGlass().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when (step.state) {
                SetupStepState.DONE -> Icons.Default.CheckCircle
                SetupStepState.CURRENT -> Icons.Default.PlayArrow
                SetupStepState.WAITING -> Icons.Default.RadioButtonUnchecked
            }
            Icon(icon, contentDescription = null, tint = onGradient, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(step.title, color = onGradient, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(step.body, color = onGradientMuted, fontSize = 13.sp)

                if (diagnostic != null) {
                    val reason = setupDiagnosticReason(diagnostic.kind)
                    val recoverySteps = setupDiagnosticSteps(diagnostic.kind)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        S.setupInstallFailedTitle,
                        color = androidx.compose.ui.graphics.Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (diagnostic.failingStep.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            S.setupFailingStep(diagnostic.failingStep),
                            color = onGradient,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (diagnostic.configureExitCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            S.setupConfigureExit(diagnostic.configureExitCode),
                            color = onGradient,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (diagnostic.lockHolder.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            S.setupLockHolder(diagnostic.lockHolder),
                            color = onGradient,
                            fontSize = 12.sp
                        )
                    }
                    if (diagnostic.pendingPackages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            S.setupPendingPackages(diagnostic.pendingPackages.joinToString(", ")),
                            color = onGradient,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(reason, color = onGradientMuted, fontSize = 12.sp)
                    recoverySteps.forEach { s ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("\u2022 $s", color = onGradientMuted, fontSize = 12.sp)
                    }
                    if (diagnostic.logTail.isNotEmpty() && logLines.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            S.setupRecentLogTitle,
                            color = onGradient,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            diagnostic.logTail.joinToString("\n"),
                            color = onGradientMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onAction) {
                        Text(S.actionRetry, color = onGradient, fontSize = 13.sp)
                    }
                }

                if (busy) {
                    if (statusText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(statusText, color = onGradientMuted, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                } else if (step.state == SetupStepState.CURRENT) {
                    if (step.action.isNotBlank() && diagnostic == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onAction) {
                            Text(step.action, color = onGradient, fontSize = 13.sp)
                        }
                    }
                    if (step.id == "disk_space") {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onReload) {
                            Text(S.setupActionCheck, color = onGradientMuted, fontSize = 13.sp)
                        }
                    }
                }

                val displayLines = if (busy) {
                    logLines
                } else if (diagnostic != null && diagnostic.logTail.isNotEmpty()) {
                    diagnostic.logTail
                } else {
                    logLines
                }
                PluginOutputLog(
                    lines = displayLines,
                    running = busy,
                    onGradient = onGradient,
                    onGradientMuted = onGradientMuted,
                    onClear = onClearLog
                )
            }
        }
    }
}

internal fun setupDiagnosticReason(kind: InstallFailureKind): String = when (kind) {
    InstallFailureKind.PROOT_ENV -> S.setupInstallReasonProot
    InstallFailureKind.DPKG_LOCK -> S.setupInstallReasonDpkgLock
    InstallFailureKind.DPKG_STATE -> S.setupInstallReasonDpkgState
    InstallFailureKind.DPKG_CONFIGURE -> S.setupInstallReasonDpkgConfigure
    InstallFailureKind.APT_UPDATE -> S.setupInstallReasonAptUpdate
    InstallFailureKind.APT_INSTALL -> S.setupInstallReasonAptInstall
    InstallFailureKind.GENERIC -> S.setupInstallReasonGeneric
}

internal fun setupDiagnosticSteps(kind: InstallFailureKind): List<String> = when (kind) {
    InstallFailureKind.PROOT_ENV -> listOf(S.setupInstallStepReextract, S.setupInstallStepRetry)
    InstallFailureKind.DPKG_LOCK -> listOf(S.setupInstallStepWaitLock, S.setupInstallStepRetry)
    InstallFailureKind.DPKG_STATE -> listOf(S.setupInstallStepFreeDisk, S.setupInstallStepReextract)
    InstallFailureKind.DPKG_CONFIGURE -> listOf(S.setupInstallStepFreeDisk, S.setupInstallStepRetry)
    InstallFailureKind.APT_UPDATE -> listOf(S.setupInstallStepSwitchMirror, S.setupInstallStepCheckNetwork)
    InstallFailureKind.APT_INSTALL -> listOf(S.setupInstallStepFreeDisk, S.setupInstallStepSwitchMirror, S.setupInstallStepRetry)
    InstallFailureKind.GENERIC -> listOf(S.setupInstallStepRetry)
}
