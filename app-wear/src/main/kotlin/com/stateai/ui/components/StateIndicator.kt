package com.stateai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.stateai.domain.state.DisplayState
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.color

private const val COLOR_FADE_MILLIS = 600
private const val VIEWPORT = 20f
private const val LABEL_GAP = 0.4f

/**
 * The shape and color of a state, plus an optional label. Each state has its own shape so it never
 * depends on color alone: focused is a filled circle, normal a ring, overloaded a triangle,
 * exhausted a diamond, and no data (null) a thin gray ring.
 */
@Composable
fun StateIndicator(
    state: DisplayState?,
    modifier: Modifier = Modifier,
    label: String? = null,
    size: Dp = 14.dp,
    fontSize: TextUnit = StateAiDimens.Body,
    textColor: Color? = null,
    ambient: Boolean = false,
) {
    val target = when {
        ambient -> StateAiColors.AmbientIndicator
        state == null -> StateAiColors.NoData
        else -> state.color()
    }
    val color by animateColorAsState(target, tween(COLOR_FADE_MILLIS), label = "stateColor")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size * LABEL_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(size)) {
            scale(this.size.width / VIEWPORT, pivot = Offset.Zero) { drawShape(state, color) }
        }
        label?.let { Text(it, color = textColor ?: color, fontSize = fontSize, fontWeight = FontWeight.Medium) }
    }
}

private fun DrawScope.drawShape(state: DisplayState?, color: Color) {
    val center = Offset(VIEWPORT / 2, VIEWPORT / 2)
    when (state) {
        DisplayState.FOCUSED -> drawCircle(color, radius = FOCUSED_RADIUS, center = center)
        DisplayState.NORMAL -> drawCircle(color, radius = NORMAL_RADIUS, center = center, style = Stroke(NORMAL_STROKE))
        DisplayState.OVERLOADED -> drawRounded(TRIANGLE, color)
        DisplayState.EXHAUSTED -> drawRounded(DIAMOND, color)
        null -> drawCircle(color, radius = NO_DATA_RADIUS, center = center, style = Stroke(NO_DATA_STROKE))
    }
}

private fun DrawScope.drawRounded(path: Path, color: Color) {
    drawPath(path, color, style = Fill)
    drawPath(path, color, style = Stroke(width = ROUNDING_STROKE, join = StrokeJoin.Round))
}

/** Shapes in a 20-unit viewport, as in the design's StateIndicator. */
private val TRIANGLE = PathParser().parsePathString("M10 4.5L16.5 15.5H3.5Z").toPath()
private val DIAMOND = PathParser().parsePathString("M10 3.5L16.5 10L10 16.5L3.5 10Z").toPath()
private const val FOCUSED_RADIUS = 7.5f
private const val NORMAL_RADIUS = 6.25f
private const val NORMAL_STROKE = 2.5f
private const val NO_DATA_RADIUS = 6f
private const val NO_DATA_STROKE = 2f
private const val ROUNDING_STROKE = 2.6f
