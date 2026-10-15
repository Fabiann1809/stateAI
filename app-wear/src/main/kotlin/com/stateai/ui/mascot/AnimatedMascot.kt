package com.stateai.ui.mascot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.stateai.ui.ambient.LocalIsAmbient
import com.stateai.ui.components.rememberReduceMotion

/** Lumi moves only on a live screen with animations allowed: never in ambient mode or with reduced motion. */
fun shouldAnimate(isAmbient: Boolean, reduceMotion: Boolean): Boolean = !isAmbient && !reduceMotion

/** The mascot blinking now and then and breathing slowly while it is idle. */
@Composable
fun AnimatedMascot(
    expression: MascotExpression,
    modifier: Modifier = Modifier,
    size: Dp = MASCOT_SIZE,
    contentDescription: String? = null,
) {
    if (!shouldAnimate(LocalIsAmbient.current, rememberReduceMotion())) {
        Mascot(expression, modifier, size = size, contentDescription = contentDescription)
        return
    }
    val transition = rememberInfiniteTransition(label = "lumi")
    val blink by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = BLINK_CYCLE_MILLIS
                0f at 0
                0f at BLINK_FIRST_START
                1f at BLINK_FIRST_SHUT
                0f at BLINK_FIRST_END
                0f at BLINK_SECOND_START
                1f at BLINK_SECOND_SHUT
                0f at BLINK_SECOND_END
            },
        ),
        label = "blink",
    )
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(BREATH_MILLIS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    Mascot(
        expression,
        modifier,
        size = size,
        blink = blink,
        breath = breath,
        contentDescription = contentDescription,
    )
}

private const val BLINK_CYCLE_MILLIS = 5200
private const val BLINK_FIRST_START = 2000
private const val BLINK_FIRST_SHUT = 2150
private const val BLINK_FIRST_END = 2300
private const val BLINK_SECOND_START = 4300
private const val BLINK_SECOND_SHUT = 4450
private const val BLINK_SECOND_END = 4600
private const val BREATH_MILLIS = 2400
