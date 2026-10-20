package com.lucent.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateCheckerTest {

    private fun info(buildId: String, tag: String = buildId): ReleaseInfo = ReleaseInfo(
        tag = tag,
        version = "3.1.2",
        title = "Preview",
        apk = ReleaseAsset("Lucent.for.Android-3.1.2.apk", "https://example.invalid/a.apk", 1L),
        installer = null,
        buildId = buildId
    )

    @Test
    fun compareBuildIdsSameWidth() {
        assertTrue(UpdateChecker.compareBuildIds("P20261019224908", "P20261019093434") > 0)
        assertTrue(UpdateChecker.compareBuildIds("P20261019093434", "P20261019224908") < 0)
        assertEquals(0, UpdateChecker.compareBuildIds("P20261019093434", "P20261019093434"))
    }

    @Test
    fun compareBuildIdsMixedWidthNormalizesToCommonPrefix() {
        assertTrue(UpdateChecker.compareBuildIds("R202610191003", "P20261019093434") > 0)
        assertTrue(UpdateChecker.compareBuildIds("P20261019093434", "R202610191003") < 0)
    }

    @Test
    fun compareBuildIdsEqualPrefixLongerWins() {
        assertTrue(UpdateChecker.compareBuildIds("P20261019100345", "R202610191003") > 0)
        assertTrue(UpdateChecker.compareBuildIds("R202610191003", "P20261019100345") < 0)
    }

    @Test
    fun compareBuildIdsUnparseable() {
        assertEquals(0, UpdateChecker.compareBuildIds("nope", "alsono"))
        assertTrue(UpdateChecker.compareBuildIds("P20261019093434", "nope") > 0)
        assertTrue(UpdateChecker.compareBuildIds("nope", "P20261019093434") < 0)
    }

    @Test
    fun isNewerBuildIdUsesNormalizedComparison() {
        assertTrue(UpdateChecker.isNewerBuildId("P20261019224908", "P20261019093434"))
        assertTrue(UpdateChecker.isNewerBuildId("R202610191003", "P20261019093434"))
        assertFalse(UpdateChecker.isNewerBuildId("P20261019093434", "P20261019224908"))
        assertFalse(UpdateChecker.isNewerBuildId("P20261019093434", "P20261019093434"))
    }

    @Test
    fun isNewerBuildIdRejectsUnparseable() {
        assertFalse(UpdateChecker.isNewerBuildId("garbage", "P20261019093434"))
        assertFalse(UpdateChecker.isNewerBuildId("P20261019093434", "garbage"))
        assertFalse(UpdateChecker.isNewerBuildId("garbage", "alsogarbage"))
    }

    @Test
    fun selectNewestPicksGreatestBuildIdRegardlessOfOrder() {
        val infos = listOf(info("R202610191003"), info("P20261019093434"), info("P20261019224908"))
        assertEquals("P20261019224908", PreviewChecker.selectNewest(infos)?.buildId)
        assertEquals("P20261019224908", PreviewChecker.selectNewest(infos.reversed())?.buildId)
    }

    @Test
    fun selectNewestEmptyIsNull() {
        assertNull(PreviewChecker.selectNewest(emptyList()))
    }

    @Test
    fun selectNewestFallsBackToTagWhenBuildIdMissing() {
        val a = info("P20261019093434").copy(buildId = null)
        val b = info("P20261019224908").copy(buildId = null)
        assertEquals("P20261019224908", PreviewChecker.selectNewest(listOf(a, b))?.tag)
    }

    @Test
    fun previewTrackFilterAcceptsPReleasesOnly() {
        assertTrue(PreviewChecker.isPreviewTrack(info("P20261019224908")))
        assertTrue(PreviewChecker.isPreviewTrack(info("P20261019224908").copy(buildId = null)))
        assertFalse(PreviewChecker.isPreviewTrack(info("R202610191003", tag = "R202610191003")))
        assertFalse(PreviewChecker.isPreviewTrack(info("R202610191003", tag = "R202610191003").copy(buildId = null)))
        assertTrue(PreviewChecker.isPreviewTrack(info("P20261019224908").copy(buildId = "a1b2c3d4e5f6")))
    }

    @Test
    fun selectNewestOverPreviewPoolSkipsStablePrerelease() {
        val pool = listOf(
            info("R202610191003", tag = "R202610191003"),
            info("P20261019093434"),
            info("P20261019224908")
        ).filter { PreviewChecker.isPreviewTrack(it) }
        assertEquals("P20261019224908", PreviewChecker.selectNewest(pool)?.buildId)
    }

    @Test
    fun sameVersionIgnoresAlphaSuffix() {
        assertTrue(UpdateChecker.sameVersion("3.1.2", "3.1.2-alpha"))
        assertTrue(UpdateChecker.sameVersion("3.1.2-alpha", "3.1.2"))
        assertTrue(UpdateChecker.sameVersion("v3.1.2", "3.1.2"))
        assertFalse(UpdateChecker.sameVersion("3.1.2", "3.1.3"))
    }

    @Test
    fun tailVetoSkipsMarketingVetoWhenBuildIdNewer() {
        assertFalse(
            UpdateChecker.tailVeto(
                digitNewer = true,
                channel = "stable",
                candidateIdentity = "3.1.2",
                candidateVersion = "3.1.2",
                currentVersion = "3.1.2"
            )
        )
        assertFalse(
            UpdateChecker.tailVeto(
                digitNewer = true,
                channel = "stable",
                candidateIdentity = "3.1.2",
                candidateVersion = "3.1.2",
                currentVersion = "3.1.2-alpha"
            )
        )
        assertFalse(
            UpdateChecker.tailVeto(
                digitNewer = true,
                channel = "preview",
                candidateIdentity = "3.1.2",
                candidateVersion = "3.1.2",
                currentVersion = "3.1.2-alpha"
            )
        )
    }

    @Test
    fun tailVetoAppliesWhenBuildIdNotNewer() {
        assertTrue(
            UpdateChecker.tailVeto(
                digitNewer = false,
                channel = "stable",
                candidateIdentity = "3.1.2",
                candidateVersion = "3.1.2",
                currentVersion = "3.1.2"
            )
        )
        assertTrue(
            UpdateChecker.tailVeto(
                digitNewer = false,
                channel = "preview",
                candidateIdentity = "3.1.2",
                candidateVersion = "3.1.1",
                currentVersion = "3.1.2"
            )
        )
    }

    @Test
    fun displayVersionAddsAlphaOnlyForPreview() {
        val preview = info("P20261019224908").copy(channel = "preview")
        assertEquals("3.1.2-alpha", preview.displayVersion)
        assertEquals("3.1.2-alpha", preview.copy(version = "3.1.2-alpha").displayVersion)
        assertEquals("3.1.2", info("R202610191003", tag = "R202610191003").copy(channel = "stable").displayVersion)
        assertEquals("3.1.2", preview.version)
    }

    @Test
    fun channelLocaleRecreateDecision() {
        assertTrue(UpdateChannelLocale.needsRecreate("", "en"))
        assertTrue(UpdateChannelLocale.needsRecreate("en", "de"))
        assertFalse(UpdateChannelLocale.needsRecreate("en", "en"))
        assertFalse(UpdateChannelLocale.needsRecreate("zh", "zh"))
    }
}
