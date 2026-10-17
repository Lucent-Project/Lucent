package com.lucent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun HomeDrawerSheet(
    mode: HomeMode,
    onSelectMode: (HomeMode) -> Unit,
    onOpenPanel: (HomePanel) -> Unit
) {
    val context = LocalContext.current
    val onGradient = LocalOnGradient.current
    val onGradientMuted = LocalOnGradientMuted.current
    val glassDark = isDarkGlass()
    val sheetShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)

    Column(
        modifier = Modifier
            .width(304.dp)
            .fillMaxHeight()
            .clip(sheetShape)
            .hazeEffect(
                state = LocalHazeState.current,
                style = HazeMaterials.thin(
                    if (glassDark) LucentGlass.HazeContainerDark else LucentGlass.HazeContainerLight
                )
            )
            .background(Color.White.copy(alpha = if (glassDark) 0.05f else 0.22f))
            .border(1.dp, lucentGlassRim(), sheetShape)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = onGradient)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Lucent", color = onGradient, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(18.dp))
        HomeModeSwitcher(mode = mode, onSelect = onSelectMode)
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            com.lucent.app.i18n.S.drawerSectionLibrary.uppercase(),
            color = onGradientMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            HomePanel.visible(HiddenArea.visible).forEach { panel ->
                DrawerRow(
                    icon = drawerIcon(panel, mode),
                    label = panel.label(mode),
                    onClick = {
                        Haptics.tick(context)
                        onOpenPanel(panel)
                    }
                )
            }
        }
        Text(
            com.lucent.app.i18n.S.drawerFooter(com.lucent.app.LucentBuild.DISPLAY_VERSION),
            color = onGradientMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 10.dp, top = 8.dp)
        )
    }
}

@Composable
private fun DrawerRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val onGradient = LocalOnGradient.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = onGradient, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = onGradient, fontSize = 15.sp, maxLines = 1, modifier = Modifier.weight(1f))
    }
}

private fun drawerIcon(panel: HomePanel, mode: HomeMode): ImageVector = when (panel) {
    HomePanel.Drafts -> Icons.Default.EditNote
    HomePanel.Archive -> if (mode == HomeMode.Tasks) Icons.Default.History else Icons.Default.Inventory2
    HomePanel.Trash -> Icons.Default.Delete
    HomePanel.Hidden -> Icons.Default.VisibilityOff
}
