package com.stateai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.stateai.ui.theme.StateAiColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Score arc of 240 degrees, open at the bottom, with ruler ticks, the progress in the accent color
 * and a white marker at the value. One color only: the score is not judged as good or bad.
 * Proportions are relative to the screen width so it fits any round watch. [score] is 0-100 or null.
 */
@Composable
fun ScoreGauge(score: Double?, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val unit = size.width
        val center = Offset(unit / 2, unit * CENTER_Y)
        val radius = unit * RADIUS
        drawTicks(center, unit)
        drawGaugeArc(center, radius, SWEEP, unit * TRACK_WIDTH, StateAiColors.Outline)
        score?.let { value ->
            val sweep = SWEEP * (value / MAX_SCORE).toFloat().coerceIn(0f, 1f)
            if (sweep > 0f) drawGaugeArc(center, radius, sweep, unit * TRACK_WIDTH, StateAiColors.Accent)
            val marker = pointAt(center, radius, START + sweep)
            drawCircle(StateAiColors.Background, radius = unit * (MARKER + MARKER_RING), center = marker)
            drawCircle(StateAiColors.Text1, radius = unit * MARKER, center = marker)
        }
    }
}

private fun DrawScope.drawGaugeArc(center: Offset, radius: Float, sweep: Float, width: Float, color: Color) {
    drawArc(
        color = color,
        startAngle = START,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawTicks(center: Offset, unit: Float) {
    repeat(TICKS + 1) { index ->
        val major = index % MAJOR_EVERY == 0
        val angle = START + SWEEP * index / TICKS
        drawLine(
            color = StateAiColors.Text3,
            start = pointAt(center, unit * TICK_INNER, angle),
            end = pointAt(center, unit * (if (major) TICK_MAJOR_OUTER else TICK_MINOR_OUTER), angle),
            strokeWidth = unit * (if (major) TICK_MAJOR_WIDTH else TICK_MINOR_WIDTH),
            cap = StrokeCap.Round,
        )
    }
}

private fun pointAt(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(center.x + radius * cos(radians).toFloat(), center.y + radius * sin(radians).toFloat())
}

private const val START = 150f
private const val SWEEP = 240f
private const val MAX_SCORE = 100.0
private const val TICKS = 20
private const val MAJOR_EVERY = 10
private const val CENTER_Y = 0.422f
private const val RADIUS = 0.298f
private const val TRACK_WIDTH = 0.04f
private const val MARKER = 0.036f
private const val MARKER_RING = 0.011f
private const val TICK_INNER = 0.347f
private const val TICK_MINOR_OUTER = 0.364f
private const val TICK_MAJOR_OUTER = 0.382f
private const val TICK_MINOR_WIDTH = 0.0045f
private const val TICK_MAJOR_WIDTH = 0.0067f
