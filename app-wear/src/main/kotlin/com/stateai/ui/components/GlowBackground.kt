package com.stateai.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.stateai.ui.theme.StateAiColors

/**
 * The app background: a very dark turquoise that fades evenly towards the edge of the round screen.
 * Livelier than flat black without saturating it, and the same on every screen. In ambient mode the
 * screen stays pure black.
 */
fun Modifier.glowBackground(ambient: Boolean): Modifier = if (ambient) {
    background(StateAiColors.Background)
} else {
    background(StateAiColors.Background).background(
        Brush.radialGradient(
            colorStops = arrayOf(
                0f to StateAiColors.GlowCenter,
                1f to StateAiColors.GlowEdge,
            ),
        ),
    )
}
