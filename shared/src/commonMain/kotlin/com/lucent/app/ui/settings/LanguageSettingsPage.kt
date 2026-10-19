package com.lucent.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.lucent.app.AppScope
import com.lucent.app.data.FontStore
import com.lucent.app.data.SettingsCache
import com.lucent.app.data.SettingsRepository
import com.lucent.app.i18n.AppLanguage
import com.lucent.app.i18n.S
import com.lucent.app.platform.LocalPlatformContext
import com.lucent.app.ui.BackHeader
import com.lucent.app.ui.GlassButton
import com.lucent.app.ui.GlassJellyTokens
import com.lucent.app.ui.LocalOnGradient
import com.lucent.app.ui.LocalOnGradientMuted
import com.lucent.app.ui.LucentFontResolver
import com.lucent.app.ui.glassJellySurface
import com.lucent.app.ui.jellyPopInAnimation
import com.lucent.app.ui.SYSTEM_FONT_KEY
import com.lucent.app.ui.SettingsRoute
import com.lucent.app.ui.frostedGlass
import kotlinx.coroutines.launch

@Composable
fun LanguageSettingsPage(
    repo: SettingsRepository,
    importedFonts: List<FontStore.FontSlot>,
    fontCanImportMore: Boolean,
    fontImporting: Boolean,
    fontError: String,
    onRequestDeleteFont: (FontStore.FontSlot) -> Unit,
    onImportFontClick: () -> Unit,
    onRoute: (SettingsRoute) -> Unit
) {
    val context = LocalPlatformContext.current
    val onGradient = LocalOnGradient.current
    val onGradientMuted = LocalOnGradientMuted.current
    val savedLanguage by repo.appLanguage.collectAsState(initial = SettingsCache.appLanguage)
    val savedFont by repo.font.collectAsState(initial = SettingsCache.font)
    var showPicker by remember { mutableStateOf(false) }

    BackHeader(onBack = { onRoute(SettingsRoute.Root) })

    Column(modifier = Modifier.fillMaxWidth().frostedGlass().padding(16.dp)) {
        Text(S.settingsFontTitle, color = onGradient, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(S.settingsFontSub, color = onGradientMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable {
                AppScope.io.launch { repo.setFont(SYSTEM_FONT_KEY) }
            }
        ) {
            RadioButton(
                selected = savedFont == SYSTEM_FONT_KEY || importedFonts.none { it.id == savedFont },
                onClick = { AppScope.io.launch { repo.setFont(SYSTEM_FONT_KEY) } }
            )
            Text(
                S.fontSystemLabel,
                color = onGradient,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        importedFonts.forEach { slot ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable {
                    AppScope.io.launch { repo.setFont(slot.id) }
                }
            ) {
                RadioButton(
                    selected = savedFont == slot.id,
                    onClick = { AppScope.io.launch { repo.setFont(slot.id) } }
                )
                Text(
                    slot.name.ifBlank { slot.fileName },
                    color = onGradient,
                    fontFamily = LucentFontResolver.resolve(context, slot.id),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(start = 10.dp)
                )
                IconButton(onClick = { onRequestDeleteFont(slot) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = S.fontDeleteA11y,
                        tint = onGradientMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (fontImporting) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(12.dp))
                Text(S.fontImporting, color = onGradientMuted, fontSize = 13.sp)
            }
        } else if (fontCanImportMore) {
            GlassButton(
                text = S.fontImportButton,
                icon = Icons.Default.Add,
                onClick = onImportFontClick
            )
        } else {
            Text(S.fontSlotsFullHint(FontStore.MAX_FONTS), color = onGradientMuted, fontSize = 12.sp)
        }
        if (fontError.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(fontError, color = Color(0xFFFFC1C1), fontSize = 13.sp)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(modifier = Modifier.fillMaxWidth().frostedGlass().padding(16.dp)) {
        Text(S.settingsLanguageTitle, color = onGradient, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        val displayName = if (savedLanguage == AppLanguage.SYSTEM.key) {
            S.langSystem
        } else {
            AppLanguage.fromKey(savedLanguage).label
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(displayName, color = onGradient, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = onGradientMuted)
        }
    }

    if (showPicker) {
        LanguagePickerPopup(
            savedLanguage = savedLanguage,
            onLanguageSelected = { key -> AppScope.io.launch { repo.setAppLanguage(key) } },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun LanguagePickerPopup(
    savedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val onGradient = LocalOnGradient.current
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                        .jellyPopInAnimation()
                        .glassJellySurface(GlassJellyTokens.DialogShape)
                        .padding(12.dp)
                ) {
                Text(
                    S.settingsLanguageTitle,
                    color = onGradient,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(modifier = Modifier.height(252.dp)) {
                    items(listOf(AppLanguage.SYSTEM) + AppLanguage.uiOrder) { lang ->
                        val name = if (lang == AppLanguage.SYSTEM) S.langSystem else lang.label
                        val select = {
                            onLanguageSelected(lang.key)
                            onDismiss()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { select() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = savedLanguage == lang.key,
                                onClick = { select() }
                            )
                            Text(
                                name,
                                color = onGradient,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
