package com.lucent.app.harness

import com.lucent.app.platform.PlatformContext
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private class SelfTestHost(private val root: File) : HarnessHost {
    override val android: Boolean = false
    override fun defaultWorkspacePath(): String = File(root, "workspace").apply { mkdirs() }.path
    override fun filesDirPath(): String = File(root, "files").apply { mkdirs() }.path
    override fun cacheDirPath(): String = File(root, "cache").apply { mkdirs() }.path
    override fun freeDiskBytes(): Long = 1024L * 1024L * 512L
    override fun capabilities(): Set<String> = setOf(HarnessRuntime.CAP_SHELL, HarnessRuntime.CAP_DEVICE)
}

private class SelfTestShell : HarnessShell {
    override val id: String = "test-shell"
    override fun isReady(): Boolean = true
    override fun describe(): String = "local test shell"

    override suspend fun run(
        command: String,
        workdir: String?,
        timeoutSeconds: Int,
        env: Map<String, String>,
        onOutput: ((String) -> Unit)?
    ): ShellOutcome {
        val isWin = System.getProperty("os.name", "").lowercase().contains("win")
        val pb = if (isWin) {
            ProcessBuilder("cmd", "/c", command)
        } else {
            ProcessBuilder("/bin/sh", "-c", command)
        }
        if (!workdir.isNullOrBlank()) {
            pb.directory(File(workdir).apply { mkdirs() })
        }
        pb.environment().putAll(env)
        val proc = pb.start()
        val finished = proc.waitFor(timeoutSeconds.toLong().coerceAtLeast(5L), TimeUnit.SECONDS)
        if (!finished) {
            proc.destroyForcibly()
            return ShellOutcome(false, "", "timed out", -1, timedOut = true)
        }
        val stdout = proc.inputStream.bufferedReader().readText()
        val stderr = proc.errorStream.bufferedReader().readText()
        onOutput?.invoke(stdout)
        return ShellOutcome(proc.exitValue() == 0, stdout, stderr, proc.exitValue(), timedOut = false)
    }
}

class PermissionSelfTestTest {

    private fun withSelfTestEnv(
        approvals: Map<String, String> = emptyMap(),
        githubToken: String = "",
        block: suspend (File, PlatformContext, String) -> Unit
    ) = runBlocking {
        val root = java.nio.file.Files.createTempDirectory("lucent-perm-selftest").toFile()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/probe") { exchange ->
            val body = "<html><head><title>Lucent SelfTest</title></head><body>Permission probe ok</body></html>"
                .toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.createContext("/rate_limit") { exchange ->
            val body = """{"resources":{"core":{"limit":60,"remaining":58}}}"""
                .toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        val baseUrl = "http://127.0.0.1:${server.address.port}"

        val prevHost = HarnessRuntime.host
        val prevShell = HarnessRuntime.shell
        val prevAndroid = HarnessRuntime.android
        val prevConfig = HarnessRuntime.config()

        HarnessRuntime.host = SelfTestHost(root)
        HarnessRuntime.shell = SelfTestShell()
        HarnessRuntime.android = false
        HarnessRuntime.install(
            HarnessConfig(
                enabled = true,
                approvals = approvals,
                githubApi = baseUrl,
                githubToken = githubToken
            )
        )
        HarnessGate.clearEscalations()
        try {
            val context = harnessTestContext()
            block(root, context, "$baseUrl/probe")
        } finally {
            server.stop(0)
            HarnessGate.clearEscalations()
            HarnessRuntime.host = prevHost
            HarnessRuntime.shell = prevShell
            HarnessRuntime.android = prevAndroid
            HarnessRuntime.install(prevConfig)
            root.deleteRecursively()
        }
    }

    @Test
    fun allTenPermissionsPassUnderAllowAndCleanUpTempFiles() {
        val allAllow = HarnessPermission.entries.associate { it.key to Approval.ALLOW.key }
        withSelfTestEnv(approvals = allAllow, githubToken = "ghp_SuperSecretTokenValue12345") { root, context, probeUrl ->
            val workspaceDir = File(HarnessRuntime.workspacePath())
            val userFile = File(workspaceDir, "my-precious-note.md")
            userFile.writeText("keep me safe")

            val results = PermissionSelfTest.runAll(
                context = context,
                httpProbeUrl = probeUrl
            )
            assertEquals(HarnessPermission.entries.size, results.size)
            results.forEach { outcome ->
                assertEquals(
                    SelfTestStatus.PASS,
                    outcome.status,
                    "${outcome.permission} failed with: ${outcome.evidence}"
                )
                assertTrue(outcome.matchesPolicy, "${outcome.permission} did not match ALLOW")
                assertTrue(outcome.evidence.isNotBlank(), "${outcome.permission} returned blank evidence")
                assertFalse(
                    outcome.evidence.contains("ghp_SuperSecretTokenValue12345"),
                    "${outcome.permission} leaked token in evidence: ${outcome.evidence}"
                )
            }

            assertTrue(userFile.exists() && userFile.readText() == "keep me safe")
            val leftoverWorkspace = workspaceDir.list()?.filter { it.startsWith(".lucent-perm-") }.orEmpty()
            assertTrue(leftoverWorkspace.isEmpty(), "leftover workspace temp files: $leftoverWorkspace")
            val cacheSelfTestDir = File(File(root, "cache"), "perm-selftest")
            val leftoverCache = cacheSelfTestDir.list()?.toList().orEmpty()
            assertTrue(leftoverCache.isEmpty(), "leftover cache temp files: $leftoverCache")
        }
    }

    @Test
    fun allTenPermissionsBlockUnderDeny() {
        val allDeny = HarnessPermission.entries.associate { it.key to Approval.DENY.key }
        withSelfTestEnv(approvals = allDeny) { _, context, probeUrl ->
            val workspaceDir = File(HarnessRuntime.workspacePath())
            val userFile = File(workspaceDir, "untouched.txt")
            userFile.writeText("still here")

            val results = PermissionSelfTest.runAll(
                context = context,
                httpProbeUrl = probeUrl
            )
            assertEquals(HarnessPermission.entries.size, results.size)
            results.forEach { outcome ->
                assertEquals(
                    SelfTestStatus.BLOCKED_DENY,
                    outcome.status,
                    "${outcome.permission} was not blocked under DENY: ${outcome.evidence}"
                )
                assertTrue(outcome.matchesPolicy)
            }
            assertTrue(userFile.exists() && userFile.readText() == "still here")
        }
    }

    @Test
    fun allTenPermissionsAskAndRespectConfirmAllowAndDecline() {
        val allConfirm = HarnessPermission.entries.associate { it.key to Approval.CONFIRM.key }
        withSelfTestEnv(approvals = allConfirm) { _, context, probeUrl ->
            val askedAllow = mutableSetOf<HarnessPermission>()
            val approvedResults = PermissionSelfTest.runAll(
                context = context,
                httpProbeUrl = probeUrl,
                confirm = { perm, _ ->
                    askedAllow.add(perm)
                    true
                }
            )
            assertEquals(HarnessPermission.entries.toSet(), askedAllow)
            approvedResults.forEach { outcome ->
                assertEquals(
                    SelfTestStatus.CONFIRMED_PASS,
                    outcome.status,
                    "${outcome.permission} failed after confirmation: ${outcome.evidence}"
                )
                assertTrue(outcome.matchesPolicy)
            }

            val askedDeny = mutableSetOf<HarnessPermission>()
            val declinedResults = PermissionSelfTest.runAll(
                context = context,
                httpProbeUrl = probeUrl,
                confirm = { perm, _ ->
                    askedDeny.add(perm)
                    false
                }
            )
            assertEquals(HarnessPermission.entries.toSet(), askedDeny)
            declinedResults.forEach { outcome ->
                assertEquals(
                    SelfTestStatus.BLOCKED_DECLINED,
                    outcome.status,
                    "${outcome.permission} was not blocked when declined: ${outcome.evidence}"
                )
                assertTrue(outcome.matchesPolicy)
            }
        }
    }

    @Test
    fun secondaryPermissionsAndOutsideWorkspacePolicyAreEnforced() {
        withSelfTestEnv(
            approvals = mapOf(
                HarnessPermission.WRITE.key to Approval.ALLOW.key,
                HarnessPermission.DELETE.key to Approval.DENY.key,
                HarnessPermission.SENSITIVE.key to Approval.ALLOW.key
            )
        ) { root, context, _ ->
            val victim = File(HarnessRuntime.workspacePath(), "keep.txt").apply { writeText("keep") }
            val batchDeleteArgs = buildJsonObject {
                put("action", "delete")
                put("paths", buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive("keep.txt")) })
            }.toString()
            val blockedDelete = HarnessGate.execute(context, null, "batch_files", batchDeleteArgs)
            assertFalse(blockedDelete.success, blockedDelete.summary)
            assertTrue(victim.exists())

            val outsideTarget = File(root, "outside-allowed/hello.txt")
            val outsideWriteArgs = buildJsonObject {
                put("path", outsideTarget.path)
                put("content", "allowed outside")
            }.toString()
            val allowedOutside = HarnessGate.execute(context, null, "write_file", outsideWriteArgs)
            assertTrue(allowedOutside.success, allowedOutside.summary)
            assertEquals("allowed outside", outsideTarget.readText())

            HarnessRuntime.install(
                HarnessRuntime.config().withApproval(HarnessPermission.SENSITIVE, Approval.DENY)
            )
            val deniedTarget = File(root, "outside-denied/nope.txt")
            val deniedEscalatedArgs = buildJsonObject {
                put("path", deniedTarget.path)
                put("content", "must not write")
                put("sandbox_permissions", "workspace-write")
                put("justification", "trying to override a denied policy")
            }.toString()
            val deniedOutside = HarnessGate.execute(context, null, "write_file", deniedEscalatedArgs)
            assertFalse(deniedOutside.success, deniedOutside.summary)
            assertFalse(deniedTarget.exists())
        }
    }

    @Test
    fun legacyConfigMigrationUnlocksDefaultGroupsAndRedactsSecrets() {
        val legacyJson = """{"enabled":true,"groups":["files","terminal","office","plan","memory","plugins"],"deviceEnabled":false}"""
        val migrated = HarnessConfig.parse(legacyJson)
        assertTrue(migrated.groupEnabled(HarnessGroup.GIT))
        assertTrue(migrated.groupEnabled(HarnessGroup.GITHUB))
        assertTrue(migrated.groupEnabled(HarnessGroup.BROWSER))
        assertTrue(migrated.groupEnabled(HarnessGroup.CONNECTORS))
        assertTrue(migrated.groupEnabled(HarnessGroup.DEVICE))
        assertTrue(migrated.deviceEnabled)

        val cfg = HarnessConfig(
            githubToken = "my_custom_gh_secret_999",
            connectors = listOf(ConnectorConfig(id = "slack", token = "xoxb-secret-slack-token"))
        )
        val raw = "Auth failed for my_custom_gh_secret_999 and xoxb-secret-slack-token and Bearer abc.def.ghi and ghp_1234567890abcdef"
        val clean = PermissionSelfTest.redactSecrets(raw, cfg)
        assertFalse(clean.contains("my_custom_gh_secret_999"))
        assertFalse(clean.contains("xoxb-secret-slack-token"))
        assertFalse(clean.contains("abc.def.ghi"))
        assertFalse(clean.contains("ghp_1234567890abcdef"))
    }
}
