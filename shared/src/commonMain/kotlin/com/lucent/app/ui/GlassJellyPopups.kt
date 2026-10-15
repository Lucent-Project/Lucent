package com.lucent.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.launch

private val LavenderAccent = Color(0xFFCBB6E8)

data class GlassJellyTokens(
    val backgroundBrush: Brush,
    val mirrorHighlightColor: Color,
    val bottomReflectionColor: Color,
    val outerBorderBrush: Brush,
    val innerGlowColor: Color,
    val shadowColor: Color,
    val isDark: Boolean,
    val lowEndFallback: Boolean
) {
    companion object {
        val DialogShape: RoundedCornerShape = RoundedCornerShape(28.dp)
        val MenuShape: RoundedCornerShape = RoundedCornerShape(20.dp)
        val BottomSheetShape: RoundedCornerShape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        val DialogElevation: Dp = 24.dp
        val MenuElevation: Dp = 16.dp
        val BottomSheetElevation: Dp = 28.dp

        val BorderWidth: Dp = 1.2.dp
        val InnerGlowWidth: Dp = 1.dp
    }
}

object GlassJellyDefaults {
    fun tokens(isDark: Boolean, lowEndFallback: Boolean = false): GlassJellyTokens {
        val extraAlpha = if (lowEndFallback) 0.10f else 0.0f
        val backgroundGradientColors = if (isDark) {
            listOf(
                Color(0xFF2B283C).copy(alpha = (0.82f + extraAlpha).coerceAtMost(1.0f)),
                Color(0xFF1D1B29).copy(alpha = (0.76f + extraAlpha).coerceAtMost(1.0f)),
                Color(0xFF252136).copy(alpha = (0.84f + extraAlpha).coerceAtMost(1.0f))
            )
        } else {
            listOf(
                Color(0xFFFFFFFF).copy(alpha = (0.84f + extraAlpha).coerceAtMost(1.0f)),
                Color(0xFFF6F3FC).copy(alpha = (0.76f + extraAlpha).coerceAtMost(1.0f)),
                Color(0xFFEDE6FA).copy(alpha = (0.82f + extraAlpha).coerceAtMost(1.0f))
            )
        }

        val backgroundBrush = Brush.verticalGradient(
            0.00f to backgroundGradientColors[0],
            0.52f to backgroundGradientColors[1],
            1.00f to backgroundGradientColors[2]
        )

        val mirrorHighlightColor = if (isDark) {
            Color.White.copy(alpha = 0.14f)
        } else {
            Color.White.copy(alpha = 0.65f)
        }

        val bottomReflectionColor = if (isDark) {
            LavenderAccent.copy(alpha = 0.05f)
        } else {
            Color(0xFFEDE6FA).copy(alpha = 0.25f)
        }

        val outerBorderBrush = if (isDark) {
            Brush.linearGradient(
                0.0f to Color.White.copy(alpha = 0.48f),
                0.5f to Color.White.copy(alpha = 0.10f),
                1.0f to LavenderAccent.copy(alpha = 0.28f),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        } else {
            Brush.linearGradient(
                0.0f to Color.White.copy(alpha = 0.92f),
                0.5f to Color.White.copy(alpha = 0.35f),
                1.0f to LavenderAccent.copy(alpha = 0.42f),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        }

        val innerGlowColor = if (isDark) {
            Color.White.copy(alpha = 0.08f)
        } else {
            Color.White.copy(alpha = 0.45f)
        }

        val shadowColor = if (isDark) {
            Color(0x66251A3A)
        } else {
            Color(0x38554277)
        }

        return GlassJellyTokens(
            backgroundBrush = backgroundBrush,
            mirrorHighlightColor = mirrorHighlightColor,
            bottomReflectionColor = bottomReflectionColor,
            outerBorderBrush = outerBorderBrush,
            innerGlowColor = innerGlowColor,
            shadowColor = shadowColor,
            isDark = isDark,
            lowEndFallback = lowEndFallback
        )
    }

    @Composable
    fun tokens(lowEndFallback: Boolean = false): GlassJellyTokens {
        return tokens(isDark = isDarkGlass(), lowEndFallback = lowEndFallback)
    }
}

fun Modifier.glassJellySurface(
    shape: Shape,
    tokens: GlassJellyTokens,
    shadowElevation: Dp,
    borderWidth: Dp = 1.2.dp
): Modifier = this
    .shadow(
        elevation = shadowElevation,
        shape = shape,
        clip = false,
        ambientColor = tokens.shadowColor,
        spotColor = tokens.shadowColor
    )
    .clip(shape)
    .background(tokens.backgroundBrush)
    .drawWithCache {
        val highlightCenter = Offset(size.width * 0.35f, size.height * -0.12f)
        val highlightRadius = (size.width * 0.85f).coerceAtLeast(1f)
        val highlightBrush = Brush.radialGradient(
            colors = listOf(tokens.mirrorHighlightColor, Color.Transparent),
            center = highlightCenter,
            radius = highlightRadius
        )
        val bottomReflectionBrush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, tokens.bottomReflectionColor),
            startY = size.height * 0.70f,
            endY = size.height
        )
        val outline = shape.createOutline(size, layoutDirection, this)
        val innerGlowWidthPx = 1.dp.toPx()

        onDrawWithContent {
            drawRect(brush = highlightBrush)
            drawRect(brush = bottomReflectionBrush)
            when (outline) {
                is Outline.Rounded -> drawRoundRect(
                    color = tokens.innerGlowColor,
                    style = Stroke(width = innerGlowWidthPx * 2f),
                    cornerRadius = outline.roundRect.topLeftCornerRadius
                )
                is Outline.Rectangle -> drawRect(
                    color = tokens.innerGlowColor,
                    style = Stroke(width = innerGlowWidthPx * 2f)
                )
                is Outline.Generic -> drawPath(
                    path = outline.path,
                    color = tokens.innerGlowColor,
                    style = Stroke(width = innerGlowWidthPx * 2f)
                )
            }
            drawContent()
        }
    }
    .border(borderWidth, tokens.outerBorderBrush, shape)

val MenuShape: RoundedCornerShape get() = GlassJellyTokens.MenuShape
val DialogShape: RoundedCornerShape get() = GlassJellyTokens.DialogShape
val BottomSheetShape: RoundedCornerShape get() = GlassJellyTokens.BottomSheetShape

@Composable
fun Modifier.glassJellySurface(
    shape: Shape,
    shadowElevation: Dp = GlassJellyTokens.MenuElevation,
    borderWidth: Dp = 1.2.dp
): Modifier = glassJellySurface(
    shape = shape,
    tokens = GlassJellyDefaults.tokens(),
    shadowElevation = shadowElevation,
    borderWidth = borderWidth
)

fun Modifier.jellyPopInAnimation(): Modifier = composed {
    val scaleX = remember { Animatable(0.84f) }
    val scaleY = remember { Animatable(0.78f) }
    val alpha = remember { Animatable(0.0f) }
    val translationY = remember { Animatable(16f) }

    LaunchedEffect(Unit) {
        launch {
            scaleX.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = 0.62f,
                    stiffness = 400f
                )
            )
        }
        launch {
            scaleY.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = 0.56f,
                    stiffness = 340f
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 150
                )
            )
        }
        launch {
            translationY.animateTo(
                targetValue = 0.0f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 380f
                )
            )
        }
    }

    graphicsLayer {
        this.scaleX = scaleX.value
        this.scaleY = scaleY.value
        this.alpha = alpha.value
        this.translationY = translationY.value
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LucentGlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
) {
    val tokens = GlassJellyDefaults.tokens()
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 560.dp)
                .fillMaxWidth()
                .jellyPopInAnimation()
                .glassJellySurface(
                    shape = GlassJellyTokens.DialogShape,
                    tokens = tokens,
                    shadowElevation = GlassJellyTokens.DialogElevation
                )
                .padding(24.dp)
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides LocalOnGradient.current
                    ) {
                        icon()
                    }
                }
            }
            if (title != null) {
                CompositionLocalProvider(
                    LocalContentColor provides LocalOnGradient.current
                ) {
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        Box(modifier = Modifier.padding(bottom = if (text != null) 16.dp else 24.dp)) {
                            title()
                        }
                    }
                }
            }
            if (text != null) {
                CompositionLocalProvider(
                    LocalContentColor provides LocalOnGradientMuted.current
                ) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        Box(modifier = Modifier.padding(bottom = 24.dp)) {
                            text()
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dismissButton != null) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                confirmButton()
            }
        }
    }
}

@Composable
fun LucentGlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    val tokens = GlassJellyDefaults.tokens()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .jellyPopInAnimation()
                .glassJellySurface(
                    shape = GlassJellyTokens.DialogShape,
                    tokens = tokens,
                    shadowElevation = GlassJellyTokens.DialogElevation
                )
                .then(modifier)
        ) {
            content()
        }
    }
}

@Composable
fun LucentGlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = GlassJellyDefaults.tokens()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .jellyPopInAnimation()
            .glassJellySurface(
                shape = GlassJellyTokens.MenuShape,
                tokens = tokens,
                shadowElevation = GlassJellyTokens.MenuElevation
            )
            .then(modifier),
        offset = offset,
        scrollState = scrollState,
        properties = properties,
        shape = GlassJellyTokens.MenuShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = null,
        content = content
    )
}

@Composable
fun LucentGlassDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors? = null,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val defaultTextColor = if (isDestructive) DANGER_RED else LocalOnGradient.current
    val defaultIconColor = if (isDestructive) DANGER_RED else LocalOnGradient.current
    val defaultTrailingColor = if (isDestructive) DANGER_RED else LocalOnGradientMuted.current
    val itemColors = colors ?: MenuDefaults.itemColors(
        textColor = defaultTextColor,
        leadingIconColor = defaultIconColor,
        trailingIconColor = defaultTrailingColor,
        disabledTextColor = defaultTextColor.copy(alpha = 0.38f),
        disabledLeadingIconColor = defaultIconColor.copy(alpha = 0.38f),
        disabledTrailingIconColor = defaultTrailingColor.copy(alpha = 0.38f)
    )
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = itemColors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}

@Composable
fun LucentGlassDragHandle(
    modifier: Modifier = Modifier
) {
    val isDark = isDarkGlass()
    val handleFillColor = if (isDark) {
        Color.White.copy(alpha = 0.28f)
    } else {
        LocalOnGradient.current.copy(alpha = 0.20f)
    }
    val handleBorderColor = if (isDark) {
        Color.White.copy(alpha = 0.40f)
    } else {
        Color.White.copy(alpha = 0.60f)
    }
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .size(width = 36.dp, height = 4.5.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(handleFillColor)
            .border(0.75.dp, handleBorderColor, RoundedCornerShape(percent = 50))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LucentGlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = GlassJellyDefaults.tokens()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = GlassJellyTokens.BottomSheetShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .jellyPopInAnimation()
                .glassJellySurface(
                    shape = GlassJellyTokens.BottomSheetShape,
                    tokens = tokens,
                    shadowElevation = GlassJellyTokens.BottomSheetElevation
                )
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LucentGlassDragHandle()
            content()
        }
    }
}
