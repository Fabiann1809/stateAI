package com.stateai.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.stateai.ui.theme.StateAiColors

private const val CENTER_Y = 0.34f
private const val MIDDLE_STOP = 0.5f
private const val CYAN_X = 0.18f
private const val CYAN_Y = 0.22f
private const val CYAN_RADIUS = 0.55f
private const val VIOLET_X = 0.86f
private const val VIOLET_Y = 0.92f
private const val VIOLET_RADIUS = 0.6f

/**
 * The app background, the same on every screen: a deep violet that fades to the edge of the round
 * screen, with a turquoise glow at the top left and a violet one at the bottom right. In ambient
 * mode the screen stays pure black.
 */
fun Modifier.glowBackground(ambient: Boolean): Modifier = if (ambient) {
    background(Color.Black)
} else {
    background(StateAiColors.Background).drawBehind { drawAurora() }
}

private fun DrawScope.drawAurora() {
    val width = size.width
    drawRect(
        Brush.radialGradient(
            colorStops = arrayOf(
                0f to StateAiColors.AuroraCenter,
                MIDDLE_STOP to StateAiColors.AuroraMiddle,
                1f to StateAiColors.AuroraEdge,
            ),
            center = Offset(width / 2, size.height * CENTER_Y),
            radius = width,
        ),
    )
    drawBlob(StateAiColors.BlobCyan, Offset(width * CYAN_X, size.height * CYAN_Y), width * CYAN_RADIUS)
    drawBlob(StateAiColors.BlobViolet, Offset(width * VIOLET_X, size.height * VIOLET_Y), width * VIOLET_RADIUS)
}

private fun DrawScope.drawBlob(color: Color, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius),
        radius = radius,
        center = center,
    )
}
