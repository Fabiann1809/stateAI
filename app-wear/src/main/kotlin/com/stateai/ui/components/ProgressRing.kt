package com.stateai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

private const val FULL_TURN = 360f
private const val TOP = -90f
private const val PROGRESS_ALPHA = 0.6f

/** Thin ring along the screen edge: a dim track and the block progress in the accent color. */
@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val stroke = StateAiDimens.RingStroke.toPx()
        val inset = stroke / 2 + 1f
        val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
        val topLeft = Offset(inset, inset)
        drawArc(StateAiColors.Outline, 0f, FULL_TURN, false, topLeft, arcSize, style = Stroke(stroke))
        val sweep = FULL_TURN * progress.coerceIn(0f, 1f)
        if (sweep > 0f) {
            drawArc(
                color = StateAiColors.Accent.copy(alpha = PROGRESS_ALPHA),
                startAngle = TOP,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
    }
}
