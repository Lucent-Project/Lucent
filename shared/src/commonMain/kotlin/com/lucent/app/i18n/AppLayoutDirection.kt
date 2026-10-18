package com.lucent.app.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

fun rtlForLanguageKey(key: String?): Boolean {
    val lang = AppLanguage.fromKey(key)
    val resolved = if (lang == AppLanguage.SYSTEM) AppLanguage.systemDefault() else lang
    return resolved == AppLanguage.AR
}

@Composable
fun AppLayoutDirection(languageKey: String?, content: @Composable () -> Unit) {
    val rtl = rtlForLanguageKey(languageKey)
    CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        content()
    }
}
