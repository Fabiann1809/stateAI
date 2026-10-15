package com.stateai.ui.voice

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stateai.ui.theme.StateAiColors

/** Two rings that swell and fade around the mascot while the microphone is open. */
@Composable
fun ListeningRings(size: Dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "listening")
    val first by transition.animateFloat(
        initialValue = START_SCALE,
        targetValue = END_SCALE,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "first",
    )
    val second by transition.animateFloat(
        initialValue = START_SCALE,
        targetValue = END_SCALE,
        animationSpec = infiniteRepeatable(
            tween(PULSE_MILLIS, delayMillis = PULSE_MILLIS / 2, easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "second",
    )
    Canvas(modifier.size(size)) {
        listOf(first, second).forEach { scale ->
            val fade = 1f - (scale - START_SCALE) / (END_SCALE - START_SCALE)
            drawCircle(
                color = StateAiColors.Accent.copy(alpha = fade * MAX_ALPHA),
                radius = this.size.minDimension / 2 * scale,
                style = Stroke(RING_WIDTH.toPx()),
            )
        }
    }
}

private val RING_WIDTH = 1.5.dp
private const val START_SCALE = 0.55f
private const val END_SCALE = 1f
private const val MAX_ALPHA = 0.8f
private const val PULSE_MILLIS = 1800
