package com.stateai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.stateai.domain.state.DisplayState
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.color

private const val FULL_TURN = 360f
private const val TOP = -90f
private const val GAP_DEGREES = 1f
private const val NO_DATA_ALPHA = 0.7f
private const val REMAINING_ALPHA = 0.5f
private val STROKE = 10.dp
private val EDGE_INSET = 11.dp

/**
 * Session timeline around the screen: one arc per minute colored by its state, gray when there was
 * no estimate, and dim outline arcs for the planned minutes that were not reached.
 */
@Composable
fun SegmentedRing(minutes: List<DisplayState?>, plannedMinutes: Int, modifier: Modifier = Modifier) {
    val total = maxOf(minutes.size, plannedMinutes, 1)
    Canvas(modifier.fillMaxSize()) {
        val stroke = STROKE.toPx()
        val radius = size.minDimension / 2 - EDGE_INSET.toPx()
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)
        val sweep = FULL_TURN / total
        repeat(total) { index ->
            drawArc(
                color = colorOf(minutes, index),
                startAngle = TOP + index * sweep + GAP_DEGREES / 2,
                sweepAngle = (sweep - GAP_DEGREES).coerceAtLeast(GAP_DEGREES / 2),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke),
            )
        }
    }
}

private fun colorOf(minutes: List<DisplayState?>, index: Int): Color = when {
    index >= minutes.size -> StateAiColors.Outline.copy(alpha = REMAINING_ALPHA)
    else -> minutes[index]?.color() ?: StateAiColors.NoData.copy(alpha = NO_DATA_ALPHA)
}
