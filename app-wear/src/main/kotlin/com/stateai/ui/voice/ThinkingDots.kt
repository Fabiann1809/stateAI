package com.stateai.ui.voice

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.stateai.ui.theme.StateAiColors

/** Three dots that hop one after the other while the mascot thinks. */
@Composable
fun ThinkingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "thinking")
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(DOT_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(StateAiColors.Accent, StateAiColors.Accent, StateAiColors.Accent2).forEachIndexed { index, color ->
            val lift by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        durationMillis = CYCLE_MILLIS
                        0f at 0
                        0f at index * STAGGER_MILLIS
                        1f at index * STAGGER_MILLIS + RISE_MILLIS
                        0f at index * STAGGER_MILLIS + 2 * RISE_MILLIS
                    },
                    RepeatMode.Restart,
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(DOT)
                    .graphicsLayer {
                        translationY = -lift * HOP
                        alpha = DIM + lift * (1f - DIM)
                    }
                    .background(color, CircleShape),
            )
        }
    }
}

private val DOT = 5.dp
private val DOT_GAP = 3.dp
private const val HOP = 8f
private const val DIM = 0.25f
private const val CYCLE_MILLIS = 1200
private const val STAGGER_MILLIS = 200
private const val RISE_MILLIS = 400
