package com.lucent.app.harness

import com.lucent.app.platform.PlatformContext
import com.lucent.app.data.AppDatabase
import com.lucent.app.data.StartupLog
import com.lucent.app.network.ToolDefinition
import com.lucent.app.network.ToolExecResult
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.*

object HarnessGate {

    @Volatile var dynamicTools: List<HarnessTool> = emptyList()

    private val groups: List<HarnessGroupTools> by lazy {
        listOf(
            FileTools,
            TerminalTools,
            OfficeDocTools,
            OfficeSheetTools,
            OfficeDeckTools,
            OfficeConvertTools,
            PdfTools,
            BrowserTools,
            DatabaseTools,
            GitTools,
            GitHubTools,
            SandboxTools,
            MemoryTools,
            PlanTools,
            TodoTools,
            GoalTools,
            AgentTools,
            SkillTools,
            McpTools,
            PluginTools,
            ConnectorTools,
            DeviceTools
        )
    }

    fun groupModules(): List<HarnessGroupTools> = groups

    fun allTools(): List<HarnessTool> = groups.flatMap { it.tools } + dynamicTools

    fun find(name: String): HarnessTool? = allTools().firstOrNull { it.name == name }

    fun isHarnessTool(name: String): Boolean = find(name) != null

    fun permissionOf(name: String): HarnessPermission? = find(name)?.permission

    fun groupOf(name: String): HarnessGroup? = find(name)?.group

    private val GIT_NETWORK_TOOLS = setOf("git_clone", "git_fetch", "git_pull", "git_push")
    private val LOCAL_PATH_GROUPS = setOf(
        HarnessGroup.FILES,
        HarnessGroup.TERMINAL,
        HarnessGroup.SANDBOX,
        HarnessGroup.OFFICE,
        HarnessGroup.PDF,
        HarnessGroup.DATA,
        HarnessGroup.GIT,
        HarnessGroup.SKILLS
    )
    private val LOCAL_READ_KEYS = setOf("from", "other", "csv_path", "source", "input")
    private val LOCAL_WRITE_KEYS = setOf("path", "to", "target", "target_dir", "output", "out", "workdir", "directory", "repo", "file", "save_to")
    private val PATCH_FILE_PREFIXES = listOf("*** Add File:", "*** Update File:", "*** Delete File:", "*** Move to:")

    fun enabledTools(android: Boolean, capabilities: Set<String> = HarnessRuntime.capabilities()): List<HarnessTool> {
        val config = HarnessRuntime.config()
        if (!config.enabled) return emptyList()
        return allTools().filter { tool ->
            config.groupEnabled(tool.group) &&
                (tool.group != HarnessGroup.DEVICE || config.deviceEnabled) &&
                tool.available(android, capabilities) &&
                supports(tool, capabilities)
        }
    }

    private fun supports(tool: HarnessTool, capabilities: Set<String>): Boolean {
        val platform = if (HarnessRuntime.android) "android" else "desktop"
        val declared = tool.requires ?: return true
        if (declared == platform) return true
        if (declared == HarnessRuntime.CAP_SHELL) return capabilities.contains(HarnessRuntime.CAP_SHELL)
        if (declared == HarnessRuntime.CAP_DEVICE) return capabilities.contains(HarnessRuntime.CAP_DEVICE)
        if (declared == HarnessRuntime.CAP_PLUGINS) return capabilities.contains(HarnessRuntime.CAP_PLUGINS)
        return capabilities.contains(declared)
    }

    fun definitions(android: Boolean, capabilities: Set<String> = HarnessRuntime.capabilities()): List<ToolDefinition> =
        enabledTools(android, capabilities).map { it.definition() }

    private fun parseArgs(argumentsJson: String): JsonObject =
        try { Json.parseToJsonElement(argumentsJson).jsonObject } catch (_: Exception) { JsonObject(emptyMap()) }

    fun requiredPermissions(
        tool: HarnessTool,
        args: JsonObject = JsonObject(emptyMap()),
        config: HarnessConfig = HarnessRuntime.config()
    ): Set<HarnessPermission> {
        val out = linkedSetOf(tool.permission)
        if (tool.group == HarnessGroup.GIT && config.approvalFor(HarnessPermission.GIT) == Approval.DENY) {
            out.add(HarnessPermission.GIT)
        }
        if (tool.group == HarnessGroup.GITHUB && config.approvalFor(HarnessPermission.GITHUB) == Approval.DENY) {
            out.add(HarnessPermission.GITHUB)
        }
        if (tool.name in GIT_NETWORK_TOOLS && config.approvalFor(HarnessPermission.NETWORK) == Approval.DENY) {
            out.add(HarnessPermission.NETWORK)
        }
        val action = ((args["action"] as? JsonPrimitive)?.contentOrNull ?: "").trim().lowercase()
        val overwrite = (args["overwrite"] as? JsonPrimitive)?.booleanOrNull ?: false
        when (tool.name) {
            "batch_files" -> if (action == "delete") out.add(HarnessPermission.DELETE)
            "move_path", "copy_path" -> if (overwrite) out.add(HarnessPermission.DELETE)
            "apply_patch" -> {
                val patch = (args["patch"] as? JsonPrimitive)?.contentOrNull.orEmpty()
                if (patch.lineSequence().any { it.trimStart().startsWith("*** Delete File:") }) {
                    out.add(HarnessPermission.DELETE)
                }
            }
            "git_branch" -> {
                val force = (args["force"] as? JsonPrimitive)?.booleanOrNull ?: false
                if (action == "delete" && force) out.add(HarnessPermission.DELETE)
            }
            "webdav_request" -> if (action == "delete") out.add(HarnessPermission.DELETE)
            "download_file" -> {
                out.add(HarnessPermission.WRITE)
                if (overwrite) out.add(HarnessPermission.DELETE)
            }
            "http_request" -> {
                val saveTo = (args["save_to"] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
                if (saveTo.isNotEmpty()) out.add(HarnessPermission.WRITE)
            }
            "db_export_csv" -> out.add(HarnessPermission.WRITE)
        }
        if (touchesOutsideWorkspace(tool, args, config)) {
            out.add(HarnessPermission.SENSITIVE)
        }
        return out
    }

    private fun touchesOutsideWorkspace(tool: HarnessTool, args: JsonObject, config: HarnessConfig): Boolean {
        val checkLocal = tool.group in LOCAL_PATH_GROUPS ||
            tool.name == "download_file" ||
            tool.name == "http_request" ||
            tool.name == "export_file"
        if (!checkLocal) return false

        fun isOutside(raw: String, write: Boolean): Boolean {
            if (raw.isBlank()) return false
            val resolved = runCatching { Workspace.resolve(raw) }.getOrNull() ?: return false
            return if (write) !Workspace.writable(config, resolved) else !Workspace.readable(config, resolved)
        }

        val defaultWrite = !tool.readOnly || tool.name == "db_export_csv"
        when (tool.name) {
            "http_request" -> {
                val saveTo = (args["save_to"] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
                return isOutside(saveTo, write = true)
            }
            "download_file" -> {
                val path = (args["path"] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
                return isOutside(path, write = true)
            }
            "export_file" -> {
                val path = (args["path"] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
                return isOutside(path, write = false)
            }
        }
        for (key in LOCAL_READ_KEYS) {
            val raw = (args[key] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
            val write = tool.name == "move_path" && key == "from"
            if (isOutside(raw, write = write)) return true
        }
        for (key in LOCAL_WRITE_KEYS) {
            val raw = (args[key] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
            val write = when {
                tool.name == "db_export_csv" && key == "path" -> false
                else -> defaultWrite
            }
            if (isOutside(raw, write = write)) return true
        }
        for (arrayKey in listOf("paths", "files")) {
            val array = args[arrayKey] as? JsonArray ?: continue
            val write = when {
                tool.name == "archive_files" -> false
                tool.name == "batch_files" -> {
                    val act = ((args["action"] as? JsonPrimitive)?.contentOrNull ?: "").trim().lowercase()
                    act != "copy"
                }
                else -> defaultWrite
            }
            for (i in 0 until array.size) {
                val raw = (array[i] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
                if (isOutside(raw, write = write)) return true
            }
        }
        if (tool.name == "apply_patch") {
            val patch = (args["patch"] as? JsonPrimitive)?.contentOrNull.orEmpty()
            patch.lineSequence().forEach { line ->
                val trimmed = line.trim()
                val prefix = PATCH_FILE_PREFIXES.firstOrNull { trimmed.startsWith(it) }
                if (prefix != null) {
                    val target = trimmed.removePrefix(prefix).trim()
                    if (isOutside(target, write = true)) return true
                }
            }
        }
        return false
    }

    fun needsConfirmation(name: String, argumentsJson: String = "{}"): Boolean {
        val tool = find(name) ?: return true
        val config = HarnessRuntime.config()
        val args = parseArgs(argumentsJson)
        return requiredPermissions(tool, args, config).any { config.approvalFor(it) == Approval.CONFIRM }
    }

    fun describe(name: String, argsJson: String): String = HarnessDescribe.describe(name, argsJson)

    private val escalations = LinkedHashSet<String>()
    private var escalationConversation = Long.MIN_VALUE

    fun escalatedInThisConversation(name: String): Boolean = withHarnessLock(escalations) {
        val conversation = HarnessRuntime.conversationId
        if (conversation != escalationConversation) {
            escalations.clear()
            escalationConversation = conversation
        }
        escalations.contains(name)
    }

    fun clearEscalations() {
        withHarnessLock(escalations) { escalations.clear() }
    }

    fun escalationProblem(tool: HarnessTool, escalation: HarnessEscalation): String? = when {
        !tool.escalatable -> "${tool.name} is not a tool that can ask for a wider sandbox."
        !HarnessEscalation.MODES.contains(escalation.sandboxPermissions) ->
            "sandbox_permissions must be ${HarnessEscalation.MODES.joinToString(" or ")}."
        escalation.justification.isBlank() -> "A retry with a wider sandbox needs a justification."
        escalatedInThisConversation(tool.name) ->
            "${tool.name} has already been retried once with a wider sandbox in this conversation."
        else -> null
    }

    fun widenedConfig(config: HarnessConfig, argsJson: String, escalation: HarnessEscalation): HarnessConfig {
        val args = try { Json.parseToJsonElement(argsJson).jsonObject } catch (e: Exception) { JsonObject(emptyMap()) }
        val fs = systemHarnessFs()
        val roots = mutableListOf<String>()
        HarnessDescribe.files(args.toString()).forEach { raw ->
            val p = runCatching { Workspace.resolveFile(raw) }.getOrNull() ?: return@forEach
            val root = if (escalation.fullAccess) fs.rootOf(p.toString()) ?: p.toString() else fs.parentOf(p.toString()) ?: p.toString()
            if (!Workspace.blocked(root)) roots.add(root)
        }
        val extra = roots.distinct()
        if (extra.isEmpty()) return config
        return config.copy(writeRoots = (config.writeRoots + extra).distinct())
    }

    private suspend fun run(name: String, ctx: HarnessCtx, args: JsonObject): Attempt = try {
        val module = groups.firstOrNull { module -> module.canHandle(name) }
        val result = module?.execute(ctx, name, args)
            ?: ToolExecResult("Nothing here can run $name.", success = false)
        Attempt(result, false)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: HarnessError) {
        Attempt(ToolExecResult(e.message ?: "${name} was refused", success = false), e.blocked)
    } catch (t: Throwable) {
        Attempt(ToolExecResult("$name failed: ${t.message ?: t::class.simpleName}", success = false), false)
    }

    private class Attempt(val result: ToolExecResult, val blocked: Boolean)

    private fun audit(
        tool: HarnessTool,
        argumentsJson: String,
        at: Long,
        approval: String,
        outcome: String,
        detail: String,
        millis: Long = 0L
    ): AuditEntry = AuditEntry(
        at = at,
        tool = tool.name,
        group = tool.group.key,
        permission = tool.permission.key,
        approval = approval,
        arguments = HarnessDescribe.digest(argumentsJson),
        outcome = outcome,
        detail = detail,
        millis = millis,
        files = HarnessDescribe.files(argumentsJson)
    )

    private suspend fun askHostConfirmation(perm: HarnessPermission, desc: String): Boolean {
        val host = HarnessRuntime.host ?: return false
        val allowOption = com.lucent.app.i18n.S.agentPermActionAllowOnce
        val denyOption = com.lucent.app.i18n.S.agentPermActionDeny
        val question = com.lucent.app.i18n.S.agentPermConfirmDialogTitle(perm.title) +
            "\n" +
            com.lucent.app.i18n.S.agentPermConfirmDialogBody(desc)
        val answer = runCatching { host.askUser(question, listOf(allowOption, denyOption)) }.getOrDefault("")
        return answer == allowOption
    }

    suspend fun execute(
        context: PlatformContext,
        db: AppDatabase?,
        name: String,
        argumentsJson: String,
        subAgentId: String? = null,
        confirmedByUser: Boolean = false,
        confirm: (suspend (HarnessTool, HarnessPermission, String) -> Boolean)? = null
    ): ToolExecResult {
        val tool = find(name) ?: return ToolExecResult("Unknown tool: $name", success = false)
        val config = HarnessRuntime.config()
        val capabilities = HarnessRuntime.capabilities()
        val android = HarnessRuntime.android
        if (!config.enabled) return ToolExecResult("The agent workspace is switched off in Settings.", success = false)
        if (!config.groupEnabled(tool.group)) {
            return ToolExecResult("The ${tool.group.title} tools are switched off in Settings.", success = false)
        }
        if (tool.group == HarnessGroup.DEVICE && !config.deviceEnabled) {
            return ToolExecResult("Device control is switched off in Settings.", success = false)
        }
        if (!tool.available(android, capabilities) || !supports(tool, capabilities)) {
            return ToolExecResult("${tool.name} needs ${tool.requires ?: "a plugin"} first. Use plugin_status or install_plugin.", success = false)
        }
        val args = parseArgs(argumentsJson)
        val required = requiredPermissions(tool, args, config)
        val started = harnessCurrentTimeMillis()
        val denied = required.firstOrNull { config.approvalFor(it) == Approval.DENY }
        if (denied != null) {
            val reason = "${denied.title} access is blocked by the permission policy, so ${tool.name} was not run."
            if (config.auditEnabled) {
                AuditTrail.record(
                    context,
                    audit(tool, argumentsJson, started, Approval.DENY.key, "blocked", reason)
                )
            }
            return ToolExecResult(reason, success = false)
        }
        val escalation = HarnessEscalation.of(args)
        if (escalation != null) {
            val problem = escalationProblem(tool, escalation)
            if (problem != null) {
                if (config.auditEnabled) {
                    AuditTrail.record(
                        context,
                        audit(tool, argumentsJson, started, "escalation-refused", "escalation-refused", problem)
                    )
                }
                return ToolExecResult(problem, success = false)
            }
        }
        val toConfirm = required.filter { config.approvalFor(it) == Approval.CONFIRM }
        var userConfirmedAny = false
        var outsideApproved = config.approvalFor(HarnessPermission.SENSITIVE) == Approval.ALLOW
        var deleteApproved = config.approvalFor(HarnessPermission.DELETE) == Approval.ALLOW
        var gitApproved = config.approvalFor(HarnessPermission.GIT) == Approval.ALLOW
        var githubApproved = config.approvalFor(HarnessPermission.GITHUB) == Approval.ALLOW
        for (perm in toConfirm) {
            if (perm == HarnessPermission.SENSITIVE && !confirmedByUser && confirm == null && subAgentId == null) {
                continue
            }
            if (config.auditEnabled) {
                AuditTrail.record(
                    context,
                    audit(tool, argumentsJson, started, "asked", "asked", "waiting for the user to allow ${tool.name}")
                )
            }
            val desc = describe(tool.name, argumentsJson)
            val allowed = when {
                confirmedByUser -> true
                confirm != null -> confirm(tool, perm, desc)
                else -> askHostConfirmation(perm, desc)
            }
            if (!allowed) {
                val reason = "${perm.title} access was not confirmed, so ${tool.name} was not run."
                if (config.auditEnabled) {
                    AuditTrail.record(
                        context,
                        audit(tool, argumentsJson, started, "declined", "blocked", reason)
                    )
                }
                return ToolExecResult(reason, success = false)
            }
            userConfirmedAny = true
            when (perm) {
                HarnessPermission.SENSITIVE -> outsideApproved = true
                HarnessPermission.DELETE -> deleteApproved = true
                HarnessPermission.GIT -> gitApproved = true
                HarnessPermission.GITHUB -> githubApproved = true
                else -> {}
            }
        }
        var ctx = HarnessCtx(
            context = context,
            db = db,
            config = config,
            capabilities = capabilities,
            android = android,
            workspacePath = HarnessRuntime.workspacePath(),
            subAgentId = subAgentId,
            conversationId = HarnessRuntime.conversationId.takeIf { it != 0L },
            enforcedGate = true,
            outsideApproved = outsideApproved,
            deleteApproved = deleteApproved,
            gitApproved = gitApproved,
            githubApproved = githubApproved
        )
        var attempt = run(name, ctx, args)
        var escalated = false
        if (
            attempt.blocked &&
            escalation != null &&
            config.approvalFor(HarnessPermission.SENSITIVE) != Approval.DENY &&
            !escalatedInThisConversation(name)
        ) {
            val wider = widenedConfig(config, argumentsJson, escalation)
            if (wider != config) {
                escalated = true
                withHarnessLock(escalations) { escalations.add(name) }
                if (config.auditEnabled) {
                    AuditTrail.record(
                        context,
                        audit(
                            tool,
                            argumentsJson,
                            harnessCurrentTimeMillis(),
                            "escalated",
                            "escalated",
                            "retrying ${tool.name} once with ${escalation.sandboxPermissions}: " +
                                escalation.justification.take(200)
                        )
                    )
                }
                ctx = ctx.copy(config = wider)
                attempt = run(name, ctx, args)
            }
        }
        val result = attempt.result
        val elapsed = harnessCurrentTimeMillis() - started
        val summary = ctx.limit(result.summary)
        val approvalTag = when {
            escalated -> "escalated"
            userConfirmedAny -> "confirmed"
            else -> config.approvalFor(tool.permission).key
        }
        if (config.auditEnabled) {
            AuditTrail.record(
                context,
                audit(
                    tool,
                    argumentsJson,
                    started,
                    approvalTag,
                    if (result.success) "ok" else "failed",
                    summary.take(400),
                    elapsed
                )
            )
        }
        runCatching {
            StartupLog.event(
                context,
                "agent tool $name (${tool.group.key}) ${if (result.success) "ok" else "failed"} in ${elapsed}ms"
            )
        }
        return if (summary == result.summary) result else result.copy(summary = summary)
    }
}
