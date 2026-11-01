package com.lucent.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucent.app.harness.HarnessConfig
import com.lucent.app.harness.HarnessRuntime
import com.lucent.app.i18n.S
import com.lucent.app.platform.LocalPlatformContext
import com.lucent.app.ui.BackHeader
import com.lucent.app.ui.DirectoryBrowse
import com.lucent.app.ui.DirectoryPickerDialog
import com.lucent.app.ui.LocalOnGradient
import com.lucent.app.ui.LocalOnGradientMuted
import com.lucent.app.ui.LucentToast
import com.lucent.app.ui.SettingsRoute
import com.lucent.app.ui.rememberPlatformStorageAccess

@Composable
fun WorkspaceSettingsPage(
    onRoute: (SettingsRoute) -> Unit,
    onGrantStorage: () -> Unit = {},
    storageGranted: Boolean = true
) {
    val context = LocalPlatformContext.current
    val onGradient = LocalOnGradient.current
    val onGradientMuted = LocalOnGradientMuted.current
    var config by remember { mutableStateOf(HarnessRuntime.config()) }
    var draft by remember { mutableStateOf(config.workspace) }
    var picking by remember { mutableStateOf(false) }

    fun update(next: HarnessConfig) {
        config = next
        HarnessRuntime.update(next)
    }

    fun apply(path: String) {
        val chosen = DirectoryBrowse.normalize(path)
        if (chosen.isEmpty()) return
        draft = chosen
        update(config.copy(workspace = chosen))
        LucentToast.show(context, S.settingsSaved)
    }

    val storageAccess = rememberPlatformStorageAccess(
        currentPath = draft.trim().ifBlank { HarnessRuntime.workspacePath() },
        onSystemPicked = { picked -> apply(picked) }
    )
    val effectiveStorageGranted = storageGranted && storageAccess.granted

    BackHeader(onBack = { onRoute(SettingsRoute.Agent) })
    Column(modifier = Modifier.fillMaxWidth()) {
        Section(onGradient, onGradientMuted, S.agentWorkspaceTitle, S.agentWorkspaceSub) {
            Text(HarnessRuntime.workspacePath(), color = onGradientMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = com.lucent.app.collapseExcessBlankLines(it) },
                label = { Text(S.agentWorkspaceLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { picking = true }) {
                    Text(S.agentWorkspacePick, color = onGradient, fontSize = 13.sp)
                }
                if (storageAccess.openSystemPicker != null) {
                    TextButton(onClick = storageAccess.openSystemPicker) {
                        Text(S.agentWorkspaceSystemPicker, color = onGradient, fontSize = 13.sp)
                    }
                }
                if (!effectiveStorageGranted) {
                    TextButton(
                        onClick = {
                            if (storageAccess.canRequestPermission) {
                                storageAccess.requestAccess()
                            } else {
                                onGrantStorage()
                            }
                        }
                    ) {
                        Text(S.agentGrantStorage, color = onGradient, fontSize = 13.sp)
                    }
                }
            }
            Row {
                TextButton(
                    enabled = draft.isNotBlank(),
                    onClick = { apply(draft) }
                ) {
                    Text(S.actionSave, color = onGradient, fontSize = 13.sp)
                }
            }
        }
    }

    if (picking) {
        DirectoryPickerDialog(
            initialPath = draft.trim().ifBlank { HarnessRuntime.workspacePath() },
            onDismiss = { picking = false },
            onOpen = { picked ->
                apply(picked)
                picking = false
            }
        )
    }
}
