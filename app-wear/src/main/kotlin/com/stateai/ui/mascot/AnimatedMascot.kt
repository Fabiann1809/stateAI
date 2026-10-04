package com.stateai.ui.mascot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.stateai.ui.ambient.LocalIsAmbient
import com.stateai.ui.components.rememberReduceMotion

/** The tip sways only on a live screen with animations allowed: never in ambient mode or with reduced motion. */
fun shouldSway(isAmbient: Boolean, reduceMotion: Boolean): Boolean = !isAmbient && !reduceMotion

/** The mascot with a slow, subtle sway of its tip (a few degrees each way) while it is idle. */
@Composable
fun AnimatedMascot(
    expression: MascotExpression,
    modifier: Modifier = Modifier,
    size: Dp = MASCOT_SIZE,
    contentDescription: String? = null,
) {
    val sway = shouldSway(LocalIsAmbient.current, rememberReduceMotion())
    val rotation = if (sway) {
        val angle by rememberInfiniteTransition(label = "tipSway").animateFloat(
            initialValue = -SWAY_DEGREES,
            targetValue = SWAY_DEGREES,
            animationSpec = infiniteRepeatable(tween(SWAY_MILLIS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "tipAngle",
        )
        angle
    } else {
        0f
    }
    Mascot(expression, modifier, size = size, tipRotation = rotation, contentDescription = contentDescription)
}

private const val SWAY_DEGREES = 4f
private const val SWAY_MILLIS = 1800
