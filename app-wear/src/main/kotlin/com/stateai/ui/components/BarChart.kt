package com.stateai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.stateai.ui.theme.StateAiColors

/**
 * Accent bars with rounded tops, evenly spread over the width. Highlighted bars are fully opaque;
 * the others are dimmer, so the eye goes to "today" or "the best hour".
 */
@Composable
fun BarChart(bars: List<Bar>, modifier: Modifier = Modifier, dimAlpha: Float = DEFAULT_DIM_ALPHA) {
    Canvas(modifier) {
        if (bars.isEmpty()) return@Canvas
        val slot = size.width / bars.size
        val width = minOf(slot * WIDTH_SHARE, MAX_WIDTH.toPx())
        bars.forEachIndexed { index, bar ->
            val left = index * slot + (slot - width) / 2
            val value = bar.value
            if (value == null) {
                drawEmptyBar(left, width)
            } else {
                val height = (size.height * value.coerceIn(0f, 1f)).coerceAtLeast(MIN_HEIGHT.toPx())
                drawPath(
                    path = topRounded(left, size.height - height, width, height),
                    color = StateAiColors.Accent.copy(alpha = if (bar.highlighted) 1f else dimAlpha),
                )
            }
        }
    }
}

private fun DrawScope.drawEmptyBar(left: Float, width: Float) {
    val height = EMPTY_HEIGHT.toPx()
    drawRoundRect(
        color = StateAiColors.NoData,
        topLeft = Offset(left, size.height - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(RADIUS.toPx()),
        style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx()))),
    )
}

private fun DrawScope.topRounded(left: Float, top: Float, width: Float, height: Float): Path {
    val radius = minOf(RADIUS.toPx(), width / 2, height)
    return Path().apply {
        addRoundRect(
            RoundRect(
                left = left,
                top = top,
                right = left + width,
                bottom = top + height,
                topLeftCornerRadius = CornerRadius(radius),
                topRightCornerRadius = CornerRadius(radius),
            ),
        )
    }
}

private const val WIDTH_SHARE = 0.62f
private const val DEFAULT_DIM_ALPHA = 0.62f
private val MAX_WIDTH = 14.dp
private val RADIUS = 6.dp
private val MIN_HEIGHT = 2.dp
private val EMPTY_HEIGHT = 20.dp
