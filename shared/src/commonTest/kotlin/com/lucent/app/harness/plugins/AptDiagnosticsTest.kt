package com.lucent.app.harness.plugins

import com.lucent.app.harness.PluginFailure
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AptDiagnosticsTest {

    @Test
    fun effectiveRegionNeverForcesChinaFromAsiaShanghaiTimezoneOrLocale() {
        val prevLocale = Locale.getDefault()
        val prevZone = TimeZone.getDefault()
        try {
            Locale.setDefault(Locale.CHINA)
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
            assertEquals("global", PluginCatalog.effectiveRegion("auto"))
            assertEquals("global", PluginCatalog.effectiveRegion("global"))
            assertEquals("cn", PluginCatalog.effectiveRegion("cn"))
            assertEquals("http://ports.ubuntu.com", PluginCatalog.aptMirror(PluginCatalog.effectiveRegion("auto")))
            assertEquals("http://mirrors.tuna.tsinghua.edu.cn", PluginCatalog.aptFallback(PluginCatalog.effectiveRegion("auto")))
        } finally {
            Locale.setDefault(prevLocale)
            TimeZone.setDefault(prevZone)
        }
    }

    @Test
    fun distinguishesAllSixFailureCategoriesAndExtractsRealLogTail() {
        val prootDiag = AptDiagnostics.analyze(
            failure = PluginFailure.INSTALL,
            message = "Node.js: the install command exited 71",
            detail = "[LUCENT_STEP:proot-preflight]\n/tmp is not writable inside the built-in environment\n[LUCENT_FAIL:PROOT_ENV:proot-preflight]"
        )
        assertEquals(InstallFailureKind.PROOT_ENV, prootDiag.kind)
        assertEquals("proot-preflight", prootDiag.failingStep)
        assertTrue(prootDiag.logTail.any { it.contains("/tmp is not writable") })

        val lockDiag = AptDiagnostics.analyze(
            failure = PluginFailure.INSTALL,
            message = "Node.js: the install command exited 73",
            detail = "[LUCENT_STEP:dpkg-configure]\nE: Could not get lock /var/lib/dpkg/lock-frontend. It is held by process 4321 (apt-get)\n[LUCENT_LOCK:PID 4321 (apt-get install -y git)]\n[LUCENT_FAIL:DPKG_LOCK:dpkg-configure]"
        )
        assertEquals(InstallFailureKind.DPKG_LOCK, lockDiag.kind)
        assertEquals("dpkg-configure", lockDiag.failingStep)
        assertTrue(lockDiag.lockHolder.contains("4321"))

        val stateDiag = AptDiagnostics.analyze(
            failure = PluginFailure.INSTALL,
            message = "Node.js: the install command exited 72",
            detail = "[LUCENT_STEP:dpkg-state-check]\ndpkg: error: unable to access the dpkg database directory /var/lib/dpkg: Read-only file system\n[LUCENT_FAIL:DPKG_STATE:dpkg-state-check]"
        )
        assertEquals(InstallFailureKind.DPKG_STATE, stateDiag.kind)
        assertEquals("dpkg-state-check", stateDiag.failingStep)
        assertTrue(stateDiag.logTail.any { it.contains("Read-only file system") })

        val configureDiag = AptDiagnostics.analyze(
            failure = PluginFailure.INSTALL,
            message = "Node.js: the install command exited 74",
            detail = "[LUCENT_STEP:dpkg-configure]\n[LUCENT_DCA_EXIT:1]\ndpkg: error processing package npm (--configure):\n dependency problems - leaving unconfigured\n[LUCENT_PENDING:libwww-perl python3-pip-whl npm]\n[LUCENT_FAIL:DPKG_CONFIGURE:dpkg-configure]"
        )
        assertEquals(InstallFailureKind.DPKG_CONFIGURE, configureDiag.kind)
        assertEquals("dpkg-configure", configureDiag.failingStep)
        assertEquals("1", configureDiag.configureExitCode)
        assertEquals(listOf("libwww-perl", "python3-pip-whl", "npm"), configureDiag.pendingPackages)

        val updateDiag = AptDiagnostics.analyze(
            failure = PluginFailure.INSTALL,
            message = "Node.js: the install command exited 75",
            detail = "[LUCENT_STEP:apt-update]\nErr:1 http://ports.ubuntu.com/ubuntu-ports noble InRelease\n  Temporary failure resolving 'ports.ubuntu.com'\n[LUCENT_FAIL:APT_UPDATE:apt-update]"
        )
        assertEquals(InstallFailureKind.APT_UPDATE, updateDiag.kind)
        assertEquals("apt-update", updateDiag.failingStep)

        val installExit100UnmetDeps = AptDiagnostics.analyze(
            failure = PluginFailure.INSTALL,
            message = "Node.js: the install command exited 100",
            detail = "Reading package lists...\nBuilding dependency tree...\nE: Unmet dependencies. Try 'apt --fix-broken install' with no packages."
        )
        assertEquals(InstallFailureKind.APT_INSTALL, installExit100UnmetDeps.kind)
        assertTrue(installExit100UnmetDeps.logTail.any { it.contains("Unmet dependencies") })
        assertFalse(installExit100UnmetDeps.logTail.any { it.startsWith("[LUCENT_") })
    }

    @Test
    fun generatedAptScriptIsResumableIdempotentAndUsesNoInstallRecommends() {
        val script = PluginCatalog.find("nodejs")!!.installScript
        assertTrue(script.contains("dpkg --configure -a"))
        assertTrue(script.contains("apt-get -f install -y --no-install-recommends"))
        assertTrue(script.contains("dpkg-query -W"))
        assertTrue(script.contains("MISSING_PKGS"))
        assertTrue(script.contains("apt-get install -y --no-install-recommends"))
        assertTrue(script.contains("{aptMirror}"))
        assertTrue(script.contains("{aptFallback}"))
        assertFalse(script.contains("apt-get clean"))
    }
}
