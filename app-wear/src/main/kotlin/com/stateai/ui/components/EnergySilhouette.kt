package com.stateai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.stateai.ui.theme.StateAiColors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Energy as an original, faceless human silhouette that empties as energy is used and refills with
 * useful pauses. The fill color is the current state; a soft wave moves on the surface unless the
 * screen is in ambient mode or the system asks to reduce motion.
 */
@Composable
fun EnergySilhouette(
    level: Double,
    color: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    style: SilhouetteStyle = SilhouetteStyle.FULL,
    low: Boolean = false,
    description: String? = null,
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0.0, MAX_LEVEL).toFloat(),
        animationSpec = tween(LEVEL_MILLIS),
        label = "energyLevel",
    )
    val fill by animateColorAsState(color, tween(COLOR_MILLIS), label = "energyColor")
    val phase by wavePhase(animated = style == SilhouetteStyle.FULL && !rememberReduceMotion())
    val semantics = description?.let { text -> Modifier.semantics { contentDescription = text } } ?: Modifier
    Canvas(modifier.size(width = height * ASPECT, height = height).then(semantics)) {
        scale(size.height / VIEW_HEIGHT, pivot = Offset.Zero) {
            when (style) {
                SilhouetteStyle.FULL -> drawFilled(animatedLevel, fill, phase, low)
                SilhouetteStyle.AMBIENT -> drawAmbient(animatedLevel)
            }
        }
    }
}

@Composable
private fun wavePhase(animated: Boolean): State<Float> {
    if (!animated) return remember { mutableFloatStateOf(0f) }
    return rememberInfiniteTransition(label = "wave").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(WAVE_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase",
    )
}

private fun DrawScope.drawFilled(level: Float, color: Color, phase: Float, low: Boolean) {
    val shape = silhouette()
    drawPath(shape, StateAiColors.Surface)
    clipPath(shape) {
        drawPath(
            path = wave(levelY(level), phase),
            brush = Brush.verticalGradient(listOf(lighten(color), color)),
        )
    }
    val outline = if (low) StateAiColors.Exhausted else OUTLINE_COLOR
    drawPath(shape, outline, style = Stroke(if (low) LOW_OUTLINE_WIDTH else OUTLINE_WIDTH, join = StrokeJoin.Round))
    NOTCHES.forEach { fraction ->
        val y = levelY(fraction * MAX_LEVEL.toFloat())
        drawLine(NOTCH_COLOR, Offset(NOTCH_START, y), Offset(NOTCH_END, y), NOTCH_WIDTH, StrokeCap.Round)
    }
}

/** The fill is a vertical gradient from a lighter tone of the state color at the surface to the color itself. */
private fun lighten(color: Color): Color = lerp(color, Color.White, LIGHTEN)

private fun DrawScope.drawAmbient(level: Float) {
    val shape = silhouette()
    clipPath(shape) {
        drawRect(StateAiColors.Ambient, Offset(0f, levelY(level) - AMBIENT_LINE / 2), Size(VIEW_WIDTH, AMBIENT_LINE))
    }
    drawPath(shape, StateAiColors.Ambient, style = Stroke(OUTLINE_WIDTH, join = StrokeJoin.Round))
}

/** Head and body in an 80 x 200 viewport (feet at y = 194). The body path closes under the head. */
private fun silhouette(): Path = Path().apply {
    addOval(Rect(center = Offset(HEAD_X, HEAD_Y), radius = HEAD_RADIUS))
    addPath(BODY)
}

private fun levelY(level: Float): Float = FEET_Y - LEVEL_SPAN * level / MAX_LEVEL.toFloat()

private fun wave(surfaceY: Float, phase: Float): Path = Path().apply {
    moveTo(-WAVE_MARGIN, surfaceY)
    var x = -WAVE_MARGIN
    while (x <= VIEW_WIDTH + WAVE_MARGIN) {
        lineTo(x, surfaceY + WAVE_AMPLITUDE * sin(2 * PI.toFloat() * x / WAVE_LENGTH + phase))
        x += WAVE_STEP
    }
    lineTo(VIEW_WIDTH + WAVE_MARGIN, VIEW_HEIGHT + WAVE_MARGIN)
    lineTo(-WAVE_MARGIN, VIEW_HEIGHT + WAVE_MARGIN)
    close()
}

private val BODY: Path = PathParser().parsePathString(
    "M35.2 33.1L35.2 39Q35 42.5 27 44Q16 46.5 14 56L10 110Q10 117 14.5 117Q19 117 19 111L22 80L25 82" +
        "L26 186Q26 194 32.5 194Q39 194 39 186L39 128L41 128L41 186Q41 194 47.5 194Q54 194 54 186L55 82" +
        "L58 80L61 111Q61 117 65.5 117Q70 117 70 110L66 56Q64 46.5 53 44Q45 42.5 44.8 39L44.8 33.1" +
        "A14 14 0 0 1 35.2 33.1Z",
).toPath()

private const val ASPECT = 0.4f
private const val VIEW_WIDTH = 80f
private const val VIEW_HEIGHT = 200f
private const val HEAD_X = 40f
private const val HEAD_Y = 20f
private const val HEAD_RADIUS = 14f
private const val FEET_Y = 194f
private const val LEVEL_SPAN = 188f
private const val MAX_LEVEL = 100.0
private const val OUTLINE_WIDTH = 1.8f
private const val LOW_OUTLINE_WIDTH = 2.4f
private const val NOTCH_WIDTH = 1.8f
private const val LIGHTEN = 0.45f
private const val NOTCH_START = 74f
private const val NOTCH_END = 79f
private val NOTCHES = listOf(0.25f, 0.5f, 0.75f)
private const val WAVE_AMPLITUDE = 3.5f
private const val WAVE_LENGTH = 40f
private const val WAVE_STEP = 2f
private const val WAVE_MARGIN = 10f
private const val WAVE_MILLIS = 3200
private const val LEVEL_MILLIS = 800
private const val COLOR_MILLIS = 600
private const val AMBIENT_LINE = 3f
private val OUTLINE_COLOR = Color.White.copy(alpha = 0.24f)
private val NOTCH_COLOR = Color.White.copy(alpha = 0.28f)
