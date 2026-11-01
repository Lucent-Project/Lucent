package com.lucent.app.ui

import kotlin.test.Test
import kotlin.test.assertTrue

class ChatBubbleWidthTest {

    @Test
    fun assistantBubbleScalesAcrossPhoneLandscapeTabletAndDesktop() {
        val phone = computeChatBubbleWidths(360f)
        assertTrue(phone.assistantMinDp >= 220f, "phone min width too narrow: ${phone.assistantMinDp}")
        assertTrue(phone.assistantMaxDp in 330f..360f, "phone max width unexpected: ${phone.assistantMaxDp}")
        assertTrue(phone.assistantMinDp <= phone.assistantMaxDp)
        assertTrue(phone.userMaxDp in 290f..360f)

        val landscape = computeChatBubbleWidths(640f)
        assertTrue(landscape.assistantMinDp >= 300f)
        assertTrue(landscape.assistantMaxDp > phone.assistantMaxDp)
        assertTrue(landscape.assistantMinDp <= landscape.assistantMaxDp)
        assertTrue(landscape.assistantMaxDp <= 640f)

        val tablet = computeChatBubbleWidths(800f)
        assertTrue(tablet.assistantMinDp >= 360f)
        assertTrue(tablet.assistantMaxDp > landscape.assistantMaxDp)
        assertTrue(tablet.assistantMinDp <= tablet.assistantMaxDp)
        assertTrue(tablet.assistantMaxDp <= 800f)

        val desktop = computeChatBubbleWidths(1280f)
        assertTrue(desktop.assistantMinDp >= 400f)
        assertTrue(desktop.assistantMaxDp in 800f..860f)
        assertTrue(desktop.assistantMinDp <= desktop.assistantMaxDp)
        assertTrue(desktop.userMaxDp <= 680f)
    }

    @Test
    fun tinyWindowClampsSafelyWithoutInvertedBounds() {
        for (width in listOf(0f, 80f, 150f, 220f, 300f)) {
            val bounds = computeChatBubbleWidths(width)
            assertTrue(bounds.assistantMinDp <= bounds.assistantMaxDp, "inverted bounds at $width: $bounds")
            assertTrue(bounds.userMaxDp > 0f)
        }
    }
}
