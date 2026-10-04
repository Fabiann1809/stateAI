package com.stateai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.stateai.domain.state.DisplayState
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.color

/**
 * States through the day as colored shapes joined by a gray line, with faint guides for each level
 * and for the hour marks at [gridX] (0-1). Levels go from focused (top) to overloaded (bottom).
 */
@Composable
fun DotLineChart(points: List<ChartPoint>, gridX: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(DASH_ON.toPx(), DASH_OFF.toPx()))
        val pad = POINT_RING.toPx()
        fun xOf(fraction: Float) = pad + fraction * (size.width - 2 * pad)
        fun yOf(state: DisplayState) = pad + LEVELS.indexOf(state) * (size.height - 2 * pad) / (LEVELS.size - 1)

        val guide = 1.dp.toPx()
        gridX.forEach { fraction ->
            val x = xOf(fraction)
            drawLine(StateAiColors.Outline, Offset(x, 0f), Offset(x, size.height), guide, pathEffect = dash)
        }
        LEVELS.forEach { state ->
            val y = yOf(state)
            drawLine(StateAiColors.Outline, Offset(0f, y), Offset(size.width, y), guide, pathEffect = dash)
        }
        val centers = points.map { Offset(xOf(it.x), yOf(it.state)) }
        drawJoiningLine(centers)
        points.zip(centers).forEach { (point, center) -> drawPoint(point.state, center) }
    }
}

private fun DrawScope.drawJoiningLine(centers: List<Offset>) {
    if (centers.size < 2) return
    val path = Path().apply {
        moveTo(centers.first().x, centers.first().y)
        centers.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, StateAiColors.NoData, style = Stroke(LINE.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawPoint(state: DisplayState, center: Offset) {
    val color = state.color()
    drawCircle(StateAiColors.Background, radius = POINT_RING.toPx(), center = center)
    drawCircle(color.copy(alpha = RING_ALPHA), radius = POINT_RING.toPx(), center = center, style = Stroke(1.dp.toPx()))
    drawStateShape(state, color, center, POINT.toPx())
}

private val LEVELS = listOf(DisplayState.FOCUSED, DisplayState.NORMAL, DisplayState.OVERLOADED)
private val POINT = 9.dp
private val POINT_RING = 7.dp
private val LINE = 1.5.dp
private val DASH_ON = 1.5.dp
private val DASH_OFF = 4.dp
private const val RING_ALPHA = 0.4f
