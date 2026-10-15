package com.stateai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

private val GRADIENT = Brush.linearGradient(listOf(StateAiColors.Accent2, StateAiColors.Accent))
private const val FULL_TURN = 360f
private const val TOP = -90f

/** Even with no progress the ring shows a dot where it starts. */
private const val MIN_SWEEP = 0.1f

/** Ring along the screen edge: a faint track and the progress in the violet-to-turquoise gradient, or in [color]. */
@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier, color: Color? = null) {
    val brush = color?.let { SolidColor(it) } ?: GRADIENT
    Canvas(modifier.fillMaxSize()) {
        val stroke = StateAiDimens.RingStroke.toPx()
        val inset = stroke / 2 + 1f
        val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
        val topLeft = Offset(inset, inset)
        drawArc(StateAiColors.Surface, 0f, FULL_TURN, false, topLeft, arcSize, style = Stroke(stroke))
        val sweep = (FULL_TURN * progress.coerceIn(0f, 1f)).coerceAtLeast(MIN_SWEEP)
        drawArc(
            brush = brush,
            startAngle = TOP,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}
