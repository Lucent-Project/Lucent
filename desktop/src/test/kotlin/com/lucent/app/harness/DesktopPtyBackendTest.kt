package com.lucent.app.harness

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopPtyBackendTest {

    @Test
    fun testBackendProperties() {
        assertTrue(DesktopPtyBackend.isReady())
        assertEquals("desktop shell terminal", DesktopPtyBackend.describe())
    }
}
