package com.lucent.app.harness.plugins

import com.lucent.app.harness.PluginFailure

enum class InstallFailureKind {
    PROOT_ENV,
    DPKG_LOCK,
    DPKG_STATE,
    DPKG_CONFIGURE,
    APT_UPDATE,
    APT_INSTALL,
    GENERIC
}

data class InstallDiagnostic(
    val kind: InstallFailureKind,
    val failingStep: String,
    val configureExitCode: String = "",
    val pendingPackages: List<String> = emptyList(),
    val lockHolder: String = "",
    val logTail: List<String> = emptyList()
)

object AptDiagnostics {

    private val STEP_MARKER = Regex("""\[LUCENT_STEP:([^\]]+)\]""")
    private val FAIL_MARKER = Regex("""\[LUCENT_FAIL:([A-Z_]+):([^\]]+)\]""")
    private val DCA_MARKER = Regex("""\[LUCENT_DCA_EXIT:([^\]]+)\]""")
    private val PENDING_MARKER = Regex("""\[LUCENT_PENDING:([^\]]*)\]""")
    private val LOCK_MARKER = Regex("""\[LUCENT_LOCK:([^\]]+)\]""")
    private val LEGACY_HOLDER = Regex("""\[lucent_apt\] holder ([^\r\n]+)""")

    fun analyze(
        failure: PluginFailure?,
        message: String,
        detail: String,
        liveLog: List<String> = emptyList()
    ): InstallDiagnostic {
        val rawLines = buildList {
            liveLog.forEach { block -> block.lines().forEach { add(it) } }
            detail.lines().forEach { add(it) }
            if (isEmpty() && message.isNotBlank()) {
                message.lines().forEach { add(it) }
            }
        }

        var lastStep = ""
        var explicitKind: InstallFailureKind? = null
        var explicitStep = ""
        var configureExit = ""
        var pendingPackages: List<String> = emptyList()
        var lockHolder = ""

        rawLines.forEachIndexed { idx, raw ->
            val line = raw.trim()
            STEP_MARKER.find(line)?.let { m ->
                lastStep = m.groupValues[1].trim()
            }
            FAIL_MARKER.find(line)?.let { m ->
                val parsed = runCatching { InstallFailureKind.valueOf(m.groupValues[1].trim()) }.getOrNull()
                if (parsed != null) {
                    explicitKind = parsed
                    explicitStep = m.groupValues[2].trim()
                }
            }
            DCA_MARKER.find(line)?.let { m ->
                configureExit = m.groupValues[1].trim()
            }
            if (line.contains("[FIX-M] configure exit:")) {
                val next = rawLines.getOrNull(idx + 1)?.trim().orEmpty()
                if (next.isNotEmpty() && next.all { it.isDigit() || it == '-' }) {
                    configureExit = next
                }
            }
            PENDING_MARKER.find(line)?.let { m ->
                pendingPackages = m.groupValues[1]
                    .split(Regex("""[\s,]+"""))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
            }
            LOCK_MARKER.find(line)?.let { m ->
                val holder = m.groupValues[1].trim()
                if (holder.isNotEmpty()) lockHolder = holder
            }
            LEGACY_HOLDER.find(line)?.let { m ->
                val holder = m.groupValues[1].trim()
                if (holder.isNotEmpty() && lockHolder.isEmpty()) lockHolder = holder
            }
        }

        val combinedLower = (message + "\n" + rawLines.joinToString("\n")).lowercase()
        val kind = explicitKind ?: inferKind(
            failure = failure,
            lower = combinedLower,
            lastStep = lastStep,
            configureExit = configureExit,
            lockHolder = lockHolder
        )

        val effectiveStep = when {
            explicitStep.isNotBlank() -> explicitStep
            lastStep.isNotBlank() -> lastStep
            else -> defaultStepLabel(kind)
        }

        val effectiveConfigureExit = configureExit.takeIf { it.isNotBlank() && it != "0" }.orEmpty()
        val tail = extractLogTail(rawLines, message)

        return InstallDiagnostic(
            kind = kind,
            failingStep = effectiveStep,
            configureExitCode = effectiveConfigureExit,
            pendingPackages = pendingPackages,
            lockHolder = lockHolder,
            logTail = tail
        )
    }

    private fun inferKind(
        failure: PluginFailure?,
        lower: String,
        lastStep: String,
        configureExit: String,
        lockHolder: String
    ): InstallFailureKind {
        if (
            failure == PluginFailure.NO_SHELL ||
            lower.contains("built-in runtime is not available") ||
            lower.contains("built-in environment is not set up") ||
            lower.contains("proot error") ||
            lower.contains("libproot") ||
            lower.contains("can't chroot") ||
            lower.contains("cannot execute: required file not found") ||
            lower.contains("ld-linux-aarch64.so") ||
            lower.contains("verification failed: required files missing") ||
            lower.contains("/tmp is not writable")
        ) {
            return InstallFailureKind.PROOT_ENV
        }

        if (
            lower.contains("unable to acquire the dpkg frontend lock") ||
            lower.contains("could not get lock /var/lib/dpkg") ||
            lower.contains("could not get lock /var/lib/apt") ||
            lower.contains("could not get lock /var/cache/apt") ||
            lower.contains("dpkg frontend lock was locked") ||
            lower.contains("is another process using it") ||
            (lockHolder.isNotBlank() && lower.contains("lock"))
        ) {
            return InstallFailureKind.DPKG_LOCK
        }

        if (
            lower.contains("read-only file system") ||
            lower.contains("unable to access the dpkg database directory") ||
            lower.contains("failed to open package info file '/var/lib/dpkg/status'") ||
            lower.contains("syntax error in file '/var/lib/dpkg/status'") ||
            lower.contains("updates directory /var/lib/dpkg/updates") ||
            lower.contains("no space left on device") ||
            lower.contains("not enough free space") ||
            lower.contains("dpkg status database is missing or unwritable")
        ) {
            return InstallFailureKind.DPKG_STATE
        }

        val hasNetworkFetchError =
            lower.contains("failed to fetch") ||
                lower.contains("some index files failed to download") ||
                lower.contains("temporary failure resolving") ||
                lower.contains("could not resolve") ||
                lower.contains("unable to connect to") ||
                lower.contains("connection timed out") ||
                lower.contains("network is unreachable") ||
                lower.contains("connection refused")

        if (failure == PluginFailure.DOWNLOAD) {
            return InstallFailureKind.APT_UPDATE
        }

        if (hasNetworkFetchError && (lastStep == "apt-update" || lower.contains("apt-get update") || lower.contains("inrelease") || lower.contains("packages.gz"))) {
            if (lastStep != "apt-install") {
                return InstallFailureKind.APT_UPDATE
            }
        }

        if (
            lastStep == "dpkg-configure" ||
            lower.contains("dpkg was interrupted") ||
            (configureExit.isNotBlank() && configureExit != "0" &&
                (lower.contains("dpkg: error processing package") || lower.contains("dependency problems - leaving unconfigured")))
        ) {
            if (lastStep != "apt-install") {
                return InstallFailureKind.DPKG_CONFIGURE
            }
        }

        if (hasNetworkFetchError && lastStep != "apt-install" && lastStep != "pip-install") {
            return InstallFailureKind.APT_UPDATE
        }

        if (
            lastStep == "apt-install" ||
            lastStep == "pip-install" ||
            lower.contains("unable to locate package") ||
            lower.contains("unmet dependencies") ||
            lower.contains("held broken packages") ||
            lower.contains("unable to correct problems") ||
            lower.contains("sub-process /usr/bin/dpkg returned an error code") ||
            lower.contains("no matching distribution found") ||
            (hasNetworkFetchError && (lastStep == "apt-install" || lastStep == "pip-install"))
        ) {
            return InstallFailureKind.APT_INSTALL
        }

        if (configureExit.isNotBlank() && configureExit != "0") {
            return InstallFailureKind.DPKG_CONFIGURE
        }

        return InstallFailureKind.GENERIC
    }

    private fun defaultStepLabel(kind: InstallFailureKind): String = when (kind) {
        InstallFailureKind.PROOT_ENV -> "proot-preflight"
        InstallFailureKind.DPKG_LOCK -> "dpkg-lock-check"
        InstallFailureKind.DPKG_STATE -> "dpkg-state-check"
        InstallFailureKind.DPKG_CONFIGURE -> "dpkg-configure"
        InstallFailureKind.APT_UPDATE -> "apt-update"
        InstallFailureKind.APT_INSTALL -> "apt-install"
        InstallFailureKind.GENERIC -> ""
    }

    private fun isInternalMarker(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith("[LUCENT_") ||
            trimmed.startsWith("[FIX-M]") ||
            trimmed.startsWith("[Diagnostics]")
    }

    private fun extractLogTail(rawLines: List<String>, fallbackMessage: String): List<String> {
        val cleaned = mutableListOf<String>()
        rawLines.forEach { raw ->
            val trimmed = raw.trimEnd()
            if (trimmed.isNotBlank() && !isInternalMarker(trimmed)) {
                if (cleaned.lastOrNull() != trimmed) {
                    cleaned.add(trimmed)
                }
            }
        }
        if (cleaned.isEmpty() && fallbackMessage.isNotBlank()) {
            fallbackMessage.lines()
                .map { it.trimEnd() }
                .filter { it.isNotBlank() && !isInternalMarker(it) }
                .forEach { cleaned.add(it) }
        }
        return cleaned.takeLast(14)
    }
}
