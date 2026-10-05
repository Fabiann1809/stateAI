package com.stateai.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.stateai.ui.theme.StateAiColors

/**
 * A soft, dark turquoise glow behind the main content that fades into black: livelier than flat
 * black without saturating the screen, and the edges stay black (cheap on OLED). [centerY] is the
 * glow's vertical center as a fraction of the height. In ambient mode the screen stays pure black.
 */
fun Modifier.glowBackground(ambient: Boolean, centerY: Float = DEFAULT_CENTER_Y): Modifier = if (ambient) {
    background(StateAiColors.Background)
} else {
    background(StateAiColors.Background).drawBehind {
        drawRect(
            Brush.radialGradient(
                colorStops = arrayOf(
                    0f to StateAiColors.GlowCenter,
                    GLOW_MIDDLE to StateAiColors.GlowEdge,
                    1f to StateAiColors.Background,
                ),
                center = Offset(size.width / 2, size.height * centerY),
                radius = size.minDimension * RADIUS,
            ),
        )
    }
}

private const val DEFAULT_CENTER_Y = 0.42f
private const val GLOW_MIDDLE = 0.55f
private const val RADIUS = 0.6f
