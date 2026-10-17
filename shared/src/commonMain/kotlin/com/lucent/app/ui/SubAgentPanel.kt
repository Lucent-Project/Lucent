package com.lucent.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.lucent.app.ui.LucentGlassAlertDialog
import com.lucent.app.ui.LucentGlassDropdownMenu
import com.lucent.app.ui.LucentGlassDropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lucent.app.platform.LocalPlatformContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpOffset
import com.lucent.app.ui.LucentBackHandler
import com.lucent.app.harness.SubAgent
import com.lucent.app.harness.SubAgentReports
import com.lucent.app.harness.SubAgents
import com.lucent.app.i18n.S
import kotlinx.coroutines.delay

private fun statusLabel(status: String): String = when (status) {
    "done" -> S.subAgentStatusDone
    "failed" -> S.subAgentStatusFailed
    "stopped" -> S.subAgentStop
    else -> S.subAgentStatusRunning
}

private data class SubAgentTreeNode(
    val agent: SubAgent,
    val depth: Int
)

private fun buildTreeNodes(agents: List<SubAgent>): List<SubAgentTreeNode> {
    val byParent = agents.groupBy { it.parentId }
    val result = mutableListOf<SubAgentTreeNode>()
    fun traverse(parentId: String?, depth: Int) {
        val children = byParent[parentId].orEmpty().sortedBy { it.startedAt }
        for (child in children) {
            result.add(SubAgentTreeNode(child, depth))
            traverse(child.id, depth + 1)
        }
    }
    traverse(null, 0)
    val addedIds = result.map { it.agent.id }.toSet()
    val orphaned = agents.filter { it.id !in addedIds }.sortedBy { it.startedAt }
    for (orphan in orphaned) {
        result.add(SubAgentTreeNode(orphan, 0))
    }
    return result
}

@Composable
fun SubAgentBreadcrumb(
    currentAgentId: String,
    tint: Color,
    mutedTint: Color,
    modifier: Modifier = Modifier
) {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(currentAgentId) {
        while (true) {
            delay(1200)
            tick++
        }
    }
    val chain = remember(currentAgentId, tick) {
        val result = mutableListOf<SubAgent>()
        var curr: SubAgent? = SubAgents.get(currentAgentId)
        val seen = mutableSetOf<String>()
        while (curr != null && seen.add(curr.id)) {
            result.add(curr)
            val pId = curr.parentId ?: break
            curr = SubAgents.get(pId)
        }
        result.reversed()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = S.subAgentMainAgent,
            color = mutedTint,
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { com.lucent.app.AppNavigation.clearSubAgentNav() }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
        Text(
            text = " > ",
            color = mutedTint.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        if (chain.isEmpty()) {
            Text(
                text = SubAgents.get(currentAgentId)?.displayId ?: currentAgentId,
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        } else {
            chain.forEachIndexed { index, agent ->
                val isCurrent = index == chain.lastIndex
                if (isCurrent) {
                    Text(
                        text = agent.displayId,
                        color = tint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = agent.displayId,
                        color = mutedTint,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val ancestors = chain.take(index).map { it.id }
                                com.lucent.app.AppNavigation.navigateToSubAgentChain(agent.id, ancestors)
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Text(
                        text = " > ",
                        color = mutedTint.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SubAgentTreePopup(
    conversationId: Long?,
    tint: Color,
    mutedTint: Color,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onReturnToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(conversationId) {
        while (true) {
            delay(1200)
            if (SubAgents.running(conversationId) > 0) tick++
        }
    }
    val agents = remember(conversationId, tick) { SubAgents.list(conversationId) }
    val treeNodes = remember(agents) { buildTreeNodes(agents) }
    val hasFinished = remember(agents) { agents.any { it.status != "running" } }

    LaunchedEffect(agents.isEmpty()) {
        if (agents.isEmpty()) {
            onDismiss()
        }
    }

    LucentGlassDropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        offset = DpOffset((-96).dp, 0.dp),
        modifier = modifier.widthIn(min = 220.dp, max = 340.dp)
    ) {
        LucentGlassDropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(S.subAgentMainConversation, color = tint, fontSize = 13.sp)
                }
            },
            onClick = onReturnToMain
        )

        treeNodes.forEach { node ->
            val agent = node.agent
            val finished = agent.status != "running"
            LucentGlassDropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.padding(start = (node.depth * 14).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(agent.displayId, color = tint, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("·", color = mutedTint, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusLabel(agent.status), color = mutedTint, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            agent.task.replace('\n', ' '),
                            color = mutedTint,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                onClick = { onNavigate(agent.id) },
                trailingIcon = if (finished) {
                    {
                        IconButton(
                            onClick = {
                                SubAgents.remove(agent.id)
                                if (com.lucent.app.AppNavigation.subAgentPageId == agent.id) {
                                    com.lucent.app.AppNavigation.backFromSubAgent()
                                }
                                tick++
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = S.actionDismiss,
                                tint = mutedTint,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else null
            )
        }

        if (hasFinished) {
            LucentGlassDropdownMenuItem(
                text = {
                    Text(S.subAgentClearFinished, fontSize = 12.sp)
                },
                isDestructive = true,
                onClick = {
                    SubAgents.removeFinished(conversationId)
                    tick++
                }
            )
        }
    }
}

@Composable
fun SubAgentChip(
    conversationId: Long?,
    tint: Color,
    mutedTint: Color,
    modifier: Modifier = Modifier
) {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(conversationId) {
        while (true) {
            delay(1200)
            if (SubAgents.running(conversationId) > 0) tick++
        }
    }
    val total = remember(conversationId, tick) { SubAgents.list(conversationId).size }

    if (total == 0) return

    var showTreePopup by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        HeaderPanelChip(
            icon = Icons.Default.Groups,
            contentDescription = S.agentSubAgents,
            onClick = { showTreePopup = !showTreePopup },
            tint = tint,
            badge = null
        )
        if (showTreePopup) {
            SubAgentTreePopup(
                conversationId = conversationId,
                tint = tint,
                mutedTint = mutedTint,
                onDismiss = { showTreePopup = false },
                onNavigate = { id ->
                    showTreePopup = false
                    com.lucent.app.AppNavigation.openSubAgent(id)
                },
                onReturnToMain = {
                    showTreePopup = false
                    com.lucent.app.AppNavigation.clearSubAgentNav()
                }
            )
        }
    }
}

@Composable
private fun EmptyAgentDialog(
    tint: Color,
    mutedTint: Color,
    onDismiss: () -> Unit
) {
    LucentGlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(S.agentSubAgents, color = tint, fontSize = 15.sp) },
        text = { Text(S.subAgentEmpty, color = mutedTint, fontSize = 12.sp) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(S.actionClose, color = tint) } }
    )
}

private sealed interface SubAgentTranscriptItem {
    data class ToolGroup(val lines: List<String>) : SubAgentTranscriptItem
    data class Single(val line: String) : SubAgentTranscriptItem
}

private fun groupTranscript(lines: List<String>): List<SubAgentTranscriptItem> {
    if (lines.isEmpty()) return emptyList()
    val result = mutableListOf<SubAgentTranscriptItem>()
    val toolBuffer = mutableListOf<String>()

    fun flushTools() {
        if (toolBuffer.isNotEmpty()) {
            result.add(SubAgentTranscriptItem.ToolGroup(toolBuffer.toList()))
            toolBuffer.clear()
        }
    }

    for (line in lines) {
        if (line.startsWith("tool ")) {
            toolBuffer.add(line)
        } else {
            flushTools()
            result.add(SubAgentTranscriptItem.Single(line))
        }
    }
    flushTools()
    return result
}

@Composable
private fun SubAgentToolBlock(
    tools: List<String>,
    tint: Color,
    mutedTint: Color,
    modifier: Modifier = Modifier
) {
    if (tools.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = mutedTint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(S.agentTraceTitle, color = mutedTint, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(S.agentTraceStepCount(tools.size), color = mutedTint, fontSize = 11.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = mutedTint,
                modifier = Modifier.size(16.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                tools.forEach { line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(tint.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                            .border(1.dp, mutedTint.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = mutedTint,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            line,
                            color = mutedTint,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubAgentDialog(
    agent: SubAgent,
    tint: Color,
    mutedTint: Color,
    onDismiss: () -> Unit
) {
    val context = LocalPlatformContext.current
    var tick by remember { mutableStateOf(0) }
    var instruction by remember { mutableStateOf("") }
    val current = remember(tick) { SubAgents.get(agent.id) ?: agent }

    LaunchedEffect(agent.id) {
        while (true) {
            delay(1000)
            tick++
        }
    }

    LucentGlassAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(current.displayId + " · " + statusLabel(current.status), color = tint, fontSize = 15.sp)
                Text(S.subAgentRounds(current.rounds), color = mutedTint, fontSize = 11.sp)
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                Text(current.task, color = tint, fontSize = 13.sp)
                if (current.inbox.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(S.subAgentInbox, color = mutedTint, fontSize = 11.sp)
                    current.inbox.forEach { line ->
                        Text(line, color = mutedTint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(S.subAgentWork, color = mutedTint, fontSize = 11.sp)
                groupTranscript(current.transcriptLines()).forEach { item ->
                    when (item) {
                        is SubAgentTranscriptItem.ToolGroup -> {
                            SubAgentToolBlock(
                                tools = item.lines,
                                tint = tint,
                                mutedTint = mutedTint,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        is SubAgentTranscriptItem.Single -> {
                            val line = item.line
                            if (line.startsWith("thinking: ")) {
                                AssistantReasoningBlock(
                                    reasoning = line.removePrefix("thinking: "),
                                    tint = tint,
                                    mutedTint = mutedTint,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            } else {
                                Text(line, color = mutedTint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
                if (current.result.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(S.subAgentResult, color = mutedTint, fontSize = 11.sp)
                    Text(current.result, color = tint, fontSize = 12.sp)
                }
                if (current.status == "running") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = instruction,
                            onValueChange = { instruction  = com.lucent.app.collapseExcessBlankLines(it) },
                            placeholder = { Text(S.subAgentInstructHint, color = mutedTint, fontSize = 11.sp) },
                            maxLines = 2,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            enabled = instruction.isNotBlank(),
                            onClick = {
                                SubAgents.instruct(current.id, instruction)
                                instruction = ""
                                tick++
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = S.subAgentSend,
                                tint = tint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(S.subAgentReportFolder(SubAgentReports.FOLDER), color = mutedTint, fontSize = 10.sp)
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (current.status == "running") {
                    TextButton(onClick = {
                        SubAgents.stop(current.id)
                        tick++
                    }) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = OverdueColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(S.subAgentStop, color = OverdueColor)
                    }
                }
                TextButton(onClick = {
                    val file = SubAgentReports.write(context, current)
                    LucentToast.show(context, S.subAgentReportSaved(file.name))
                }) {
                    Text(S.subAgentSaveReport, color = tint)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(S.actionClose, color = mutedTint) }
        }
    )
}

@Composable
fun SubAgentChatPage(agentId: String, tint: Color, mutedTint: Color, onBack: () -> Unit) {
    var tick by remember { mutableStateOf(0) }
    var instruction by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val current = remember(agentId, tick) { SubAgents.get(agentId) }
    LucentBackHandler(onBack = onBack)
    LaunchedEffect(agentId) {
        while (true) {
            delay(800)
            tick++
        }
    }
    if (current == null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = S.actionBack, tint = tint) }
            Text(S.subAgentEmpty, color = mutedTint)
        }
        return
    }
    Column(Modifier.fillMaxSize().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 32.dp)) {
        SubAgentBreadcrumb(currentAgentId = agentId, tint = tint, mutedTint = mutedTint)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(current.displayId + " · " + statusLabel(current.status), color = tint, fontSize = 16.sp)
                Text(current.task, color = mutedTint, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        val children = SubAgents.list(current.conversationId).filter { it.parentId == current.id }
        if (children.isNotEmpty()) {
            Text(S.agentSubAgents, color = tint, fontSize = 13.sp, modifier = Modifier.clickable { expanded = !expanded }.padding(8.dp))
            if (expanded) children.forEach { child ->
                Text(child.displayId + " · " + child.task, color = mutedTint, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
                    com.lucent.app.AppNavigation.openSubAgent(child.id)
                }.padding(start = 18.dp, top = 5.dp, bottom = 5.dp))
            }
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .background(tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(current.task, color = tint, fontSize = 13.sp)
                }
            }
            groupTranscript(current.transcriptLines()).forEach { item ->
                when (item) {
                    is SubAgentTranscriptItem.ToolGroup -> {
                        SubAgentToolBlock(
                            tools = item.lines,
                            tint = tint,
                            mutedTint = mutedTint,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    is SubAgentTranscriptItem.Single -> {
                        val line = item.line
                        when {
                            line.startsWith("parent: ") -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 480.dp)
                                            .background(tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(line.removePrefix("parent: "), color = tint, fontSize = 13.sp)
                                    }
                                }
                            }
                            line.startsWith("agent: ") -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 480.dp)
                                            .background(tint.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .border(1.dp, mutedTint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(line.removePrefix("agent: "), color = tint, fontSize = 13.sp)
                                    }
                                }
                            }
                            line.startsWith("thinking: ") -> {
                                AssistantReasoningBlock(
                                    reasoning = line.removePrefix("thinking: "),
                                    tint = tint,
                                    mutedTint = mutedTint,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            else -> {
                                Text(
                                    line,
                                    color = mutedTint,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            if (current.result.isNotBlank() && current.transcriptLines().none { it == "agent: ${current.result}" }) {
                val isError = current.status == "failed"
                val resultBg = if (isError) OverdueColor.copy(alpha = 0.10f) else tint.copy(alpha = 0.05f)
                val resultBorder = if (isError) OverdueColor.copy(alpha = 0.25f) else mutedTint.copy(alpha = 0.15f)
                val resultTextColor = if (isError) OverdueColor else tint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(resultBg, RoundedCornerShape(8.dp))
                        .border(1.dp, resultBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(current.result, color = resultTextColor, fontSize = 12.sp)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = instruction,
                onValueChange = { instruction = it },
                placeholder = { Text(S.subAgentInstructHint) },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                enabled = instruction.isNotBlank(),
                onClick = {
                    val text = instruction.trim()
                    if (text.isNotBlank()) {
                        if (current.status == "running") {
                            SubAgents.instruct(current.id, text)
                        } else {
                            SubAgents.resume(current.id, text)
                        }
                        instruction = ""
                        tick++
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = S.subAgentSend,
                    tint = if (instruction.isNotBlank()) tint else mutedTint
                )
            }
        }
    }
}
