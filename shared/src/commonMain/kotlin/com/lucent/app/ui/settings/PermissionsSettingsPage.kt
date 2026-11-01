package com.lucent.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucent.app.harness.Approval
import com.lucent.app.harness.HarnessConfig
import com.lucent.app.harness.HarnessPermission
import com.lucent.app.harness.HarnessRuntime
import com.lucent.app.harness.PermissionSelfTest
import com.lucent.app.harness.SelfTestOutcome
import com.lucent.app.harness.SelfTestStatus
import com.lucent.app.i18n.S
import com.lucent.app.platform.LocalPlatformContext
import com.lucent.app.ui.BackHeader
import com.lucent.app.ui.LocalOnGradient
import com.lucent.app.ui.LocalOnGradientMuted
import com.lucent.app.ui.LucentGlassAlertDialog
import com.lucent.app.ui.SettingsRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private class PendingSelfTestConfirm(
    val permission: HarnessPermission,
    val detail: String,
    val deferred: CompletableDeferred<Boolean>
)

@Composable
fun PermissionsSettingsPage(onRoute: (SettingsRoute) -> Unit) {
    val context = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val onGradient = LocalOnGradient.current
    val onGradientMuted = LocalOnGradientMuted.current
    var config by remember { mutableStateOf(HarnessRuntime.config()) }
    var outcomes by remember { mutableStateOf<Map<HarnessPermission, SelfTestOutcome>>(emptyMap()) }
    var runningPermission by remember { mutableStateOf<HarnessPermission?>(null) }
    var runningAll by remember { mutableStateOf(false) }
    var pendingConfirm by remember { mutableStateOf<PendingSelfTestConfirm?>(null) }

    val isBusy = runningAll || runningPermission != null

    DisposableEffect(Unit) {
        onDispose {
            pendingConfirm?.deferred?.complete(false)
        }
    }

    fun update(next: HarnessConfig) {
        config = next
        HarnessRuntime.update(next)
    }

    val confirmHandler: suspend (HarnessPermission, String) -> Boolean = { perm, detail ->
        val deferred = CompletableDeferred<Boolean>()
        withContext(Dispatchers.Main.immediate) {
            pendingConfirm = PendingSelfTestConfirm(perm, detail, deferred)
        }
        try {
            deferred.await()
        } finally {
            withContext(Dispatchers.Main.immediate) {
                if (pendingConfirm?.deferred === deferred) {
                    pendingConfirm = null
                }
            }
        }
    }

    fun launchSingleTest(permission: HarnessPermission) {
        if (isBusy) return
        runningPermission = permission
        scope.launch {
            try {
                val outcome = withContext(Dispatchers.IO) {
                    PermissionSelfTest.runOne(
                        context = context,
                        permission = permission,
                        confirm = confirmHandler
                    )
                }
                outcomes = outcomes + (permission to outcome)
            } finally {
                runningPermission = null
            }
        }
    }

    fun launchAllTests() {
        if (isBusy) return
        runningAll = true
        scope.launch {
            try {
                for (permission in HarnessPermission.entries) {
                    runningPermission = permission
                    val outcome = withContext(Dispatchers.IO) {
                        PermissionSelfTest.runOne(
                            context = context,
                            permission = permission,
                            confirm = confirmHandler
                        )
                    }
                    outcomes = outcomes + (permission to outcome)
                }
            } finally {
                runningPermission = null
                runningAll = false
            }
        }
    }

    BackHeader(onBack = { onRoute(SettingsRoute.Agent) })
    Column(modifier = Modifier.fillMaxWidth()) {
        Section(onGradient, onGradientMuted, S.agentPermSelfTestTitle, S.agentPermSelfTestSub) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { launchAllTests() },
                    enabled = !isBusy
                ) {
                    Text(
                        text = if (runningAll) S.agentPermSelfTestRunning else S.agentPermSelfTestRunAll,
                        color = if (!isBusy) onGradient else onGradientMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (outcomes.isNotEmpty()) {
                    val matched = outcomes.values.count { it.matchesPolicy }
                    Text(
                        text = "$matched / ${outcomes.size} ✓",
                        color = if (matched == outcomes.size) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Section(onGradient, onGradientMuted, S.agentPermissionsTitle, S.agentPermissionsSub) {
            HarnessPermission.entries.forEach { permission ->
                val current = config.approvalFor(permission)
                val isThisRunning = runningPermission == permission
                val outcome = outcomes[permission]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(permission.title, color = onGradient, fontSize = 14.sp)
                            Text(permission.detail, color = onGradientMuted, fontSize = 11.sp)
                        }
                        TextButton(
                            enabled = !isBusy,
                            onClick = {
                                val next = when (current) {
                                    Approval.ALLOW -> Approval.CONFIRM
                                    Approval.CONFIRM -> Approval.DENY
                                    Approval.DENY -> Approval.ALLOW
                                }
                                outcomes = outcomes - permission
                                update(PermissionSelfTest.syncConfigForPermission(config, permission, next))
                            }
                        ) {
                            Text(current.title, color = onGradient, fontSize = 13.sp)
                        }
                        TextButton(
                            enabled = !isBusy,
                            onClick = { launchSingleTest(permission) }
                        ) {
                            Text(
                                text = if (isThisRunning) S.agentPermSelfTestRunning else S.agentPermSelfTestRunOne,
                                color = if (!isBusy) onGradient else onGradientMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (outcome != null) {
                        val badgeLabel = statusLabelOf(outcome.status)
                        val badgeColor = statusColorOf(outcome)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = badgeColor.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "${outcome.toolName} · ${outcome.durationMs} ms",
                                    color = onGradientMuted,
                                    fontSize = 10.sp
                                )
                            }
                            if (outcome.evidence.isNotBlank()) {
                                Text(
                                    text = outcome.evidence,
                                    color = onGradientMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    val activeConfirm = pendingConfirm
    if (activeConfirm != null) {
        LucentGlassAlertDialog(
            onDismissRequest = { activeConfirm.deferred.complete(false) },
            title = { Text(S.agentPermConfirmDialogTitle(activeConfirm.permission.title)) },
            text = { Text(S.agentPermConfirmDialogBody(activeConfirm.detail)) },
            confirmButton = {
                TextButton(onClick = { activeConfirm.deferred.complete(true) }) {
                    Text(S.agentPermActionAllowOnce)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeConfirm.deferred.complete(false) }) {
                    Text(S.agentPermActionDeny)
                }
            }
        )
    }
}

private fun statusLabelOf(status: SelfTestStatus): String = when (status) {
    SelfTestStatus.PASS -> S.agentPermSelfTestPass
    SelfTestStatus.BLOCKED_DENY -> S.agentPermSelfTestBlocked
    SelfTestStatus.CONFIRMED_PASS -> S.agentPermSelfTestConfirmed
    SelfTestStatus.BLOCKED_DECLINED -> S.agentPermSelfTestDeclined
    SelfTestStatus.FAIL -> S.agentPermSelfTestFail
}

private fun statusColorOf(outcome: SelfTestOutcome): Color = when {
    outcome.status == SelfTestStatus.FAIL -> Color(0xFFF87171)
    outcome.status == SelfTestStatus.BLOCKED_DENY || outcome.status == SelfTestStatus.BLOCKED_DECLINED -> Color(0xFFFBBF24)
    outcome.matchesPolicy -> Color(0xFF34D399)
    else -> Color(0xFFF87171)
}
