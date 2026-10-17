package com.lucent.app.ui

actual object LucentBrowser {
    actual fun openUrl(url: String) {
        try {
            if (java.awt.Desktop.isDesktopSupported())
                java.awt.Desktop.getDesktop().browse(java.net.URI(url))
        } catch (_: Throwable) {}
    }
}
