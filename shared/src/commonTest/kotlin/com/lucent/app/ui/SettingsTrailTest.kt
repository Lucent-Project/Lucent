package com.lucent.app.ui

import com.lucent.app.ui.settings.toolkitPages
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsTrailTest {

    @Test
    fun thePluginsPageSitsBetweenWorkspaceAndCapabilities() {
        val routes = toolkitPages().map { it.route }
        val workspace = routes.indexOf(SettingsRoute.Workspace)
        val plugins = routes.indexOf(SettingsRoute.Plugins)
        val capabilities = routes.indexOf(SettingsRoute.Capabilities)
        val permissions = routes.indexOf(SettingsRoute.Permissions)
        assertEquals(true, workspace >= 0 && plugins == workspace + 1 && capabilities == plugins + 1 && permissions == capabilities + 1)
    }

    @Test
    fun theToolkitAndShizukuHangOffAdvanced() {
        assertEquals(SettingsRoute.Advanced, SettingsTrail.parent(SettingsRoute.Shizuku))
        assertEquals(SettingsRoute.Advanced, SettingsTrail.parent(SettingsRoute.Agent))
    }

    @Test
    fun everyFineSettingIsAToolkitSubPage() {
        val pages = listOf(
            SettingsRoute.Workspace,
            SettingsRoute.Capabilities,
            SettingsRoute.Permissions,
            SettingsRoute.Groups,
            SettingsRoute.Execution,
            SettingsRoute.Github,
            SettingsRoute.Mcp,
            SettingsRoute.Audit
        )
        pages.forEach { page ->
            assertEquals(SettingsRoute.Agent, SettingsTrail.parent(page), page.name)
        }
    }

    @Test
    fun pluginSettingsHangOffCapabilities() {
        assertEquals(SettingsRoute.Capabilities, SettingsTrail.parent(SettingsRoute.Plugins))
        assertEquals(SettingsRoute.Capabilities, SettingsTrail.parent(SettingsRoute.PluginSetup))
    }

    @Test
    fun theToolkitSitsBetweenTheRootAndItsSubPages() {
        assertEquals(
            listOf(SettingsRoute.Root, SettingsRoute.Advanced, SettingsRoute.Agent, SettingsRoute.Capabilities, SettingsRoute.Plugins),
            SettingsTrail.trail(SettingsRoute.Plugins)
        )
        assertEquals(
            listOf(SettingsRoute.Root, SettingsRoute.Advanced, SettingsRoute.Shizuku),
            SettingsTrail.trail(SettingsRoute.Shizuku)
        )
    }

    @Test
    fun everySubPageHasATitle() {
        SettingsRoute.entries.forEach { route ->
            assertEquals(true, SettingsTrail.title(route).isNotBlank(), route.name)
        }
    }
}
