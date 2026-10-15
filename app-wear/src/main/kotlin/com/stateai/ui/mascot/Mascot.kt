package com.stateai.ui.mascot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lumi, the mascot, in one [expression]: a small robot with a screen for a face. [blink] closes its
 * eyes (0 open, 1 closed) and [breath] swells the whole figure a little; both come from the animated
 * wrapper, so this stays a plain drawing.
 */
@Composable
fun Mascot(
    expression: MascotExpression,
    modifier: Modifier = Modifier,
    size: Dp = MASCOT_SIZE,
    blink: Float = 0f,
    breath: Float = 0f,
    contentDescription: String? = null,
) {
    val look = lookOf(expression)
    val semantics = contentDescription
        ?.let { text -> Modifier.semantics { this.contentDescription = text } }
        ?: Modifier
    Canvas(modifier.size(size).then(semantics)) {
        scale(this.size.width / VIEWPORT, pivot = Offset.Zero) {
            withTransform({
                translate(INSET, INSET)
                scale(FIGURE_SCALE, pivot = Offset.Zero)
                scale(1f + breath * BREATH_SWELL, 1f - breath * BREATH_SWELL, pivot = Offset(CENTER_X, FEET_Y))
            }) {
                drawArms(look)
                drawEars(look)
                drawBody()
                drawFace(look, blink)
            }
        }
    }
}

private fun DrawScope.drawArms(look: LumiLook) {
    arm(Offset(ARM_LEFT_X, ARM_Y), Offset(ARM_PIVOT_LEFT_X, ARM_PIVOT_Y), look.armAngle, ARM_TILT)
    arm(Offset(ARM_RIGHT_X, ARM_Y), Offset(ARM_PIVOT_RIGHT_X, ARM_PIVOT_Y), -look.armAngle, -ARM_TILT)
}

private fun DrawScope.arm(center: Offset, pivot: Offset, angle: Float, tilt: Float) {
    rotate(angle, pivot) {
        rotate(tilt, center) {
            drawOval(
                LumiShapes.Ice,
                topLeft = Offset(center.x - ARM_RX, center.y - ARM_RY),
                size = Size(ARM_RX * 2, ARM_RY * 2),
            )
        }
    }
}

private fun DrawScope.drawEars(look: LumiLook) {
    rotate(look.earAngle, Offset(EAR_LEFT_X, EAR_Y)) { drawPath(LumiShapes.ear, LumiShapes.Ice) }
    // The right ear is the left one mirrored around the axis of the body.
    withTransform({ scale(-1f, 1f, pivot = Offset(CENTER_X, 0f)) }) {
        rotate(look.earAngle, Offset(EAR_LEFT_X, EAR_Y)) { drawPath(LumiShapes.ear, LumiShapes.Ice) }
    }
}

private fun DrawScope.drawBody() {
    drawPath(LumiShapes.body, LumiShapes.Ice)
    drawPath(LumiShapes.shade, LumiShapes.Shade)
    drawOval(
        LumiShapes.Reflection,
        topLeft = Offset(CENTER_X - REFLECTION_RX, REFLECTION_Y - REFLECTION_RY),
        size = Size(REFLECTION_RX * 2, REFLECTION_RY * 2),
    )
    drawRoundRect(
        LumiShapes.Screen,
        topLeft = Offset(SCREEN_X, SCREEN_Y),
        size = Size(SCREEN_W, SCREEN_H),
        cornerRadius = CornerRadius(SCREEN_RADIUS),
    )
    drawPath(LumiShapes.glint, Color.White, style = line(GLINT_WIDTH))
}

private fun DrawScope.drawFace(look: LumiLook, blink: Float) {
    if (look.cheeks) {
        drawCircle(LumiShapes.Face, CHEEK_RADIUS, Offset(CHEEK_LEFT_X, CHEEK_Y))
        drawCircle(LumiShapes.Face, CHEEK_RADIUS, Offset(CHEEK_RIGHT_X, CHEEK_Y))
    }
    drawEyes(look.eyes, blink)
    when (val mouth = look.mouth) {
        is LumiMouth.Line -> drawPath(mouth.path, LumiShapes.Face, style = line(MOUTH_WIDTH))
        LumiMouth.Ring -> drawCircle(LumiShapes.Face, RING_RADIUS, Offset(CENTER_X, RING_Y), style = Stroke(RING_WIDTH))
    }
    if (look.sparks) LumiShapes.sparks.forEach { drawPath(it, LumiShapes.Face, style = line(SPARK_WIDTH)) }
}

private fun DrawScope.drawEyes(eyes: LumiEyes, blink: Float) {
    val open = 1f - blink * BLINK_DEPTH
    when (eyes) {
        is LumiEyes.Pills -> scale(1f, open, pivot = Offset(CENTER_X, eyes.top + eyes.height / 2)) {
            listOf(EYE_LEFT_X, EYE_RIGHT_X).forEach { x ->
                drawRoundRect(
                    LumiShapes.Face,
                    topLeft = Offset(x, eyes.top),
                    size = Size(EYE_WIDTH, eyes.height),
                    cornerRadius = CornerRadius(EYE_WIDTH / 2),
                )
            }
        }
        LumiEyes.Narrowed -> scale(1f, open, pivot = Offset(CENTER_X, NARROWED_CENTER_Y)) {
            listOf(LumiShapes.narrowedLeft, LumiShapes.narrowedRight).forEach { shape ->
                drawPath(shape, LumiShapes.Face, style = Fill)
                drawPath(shape, LumiShapes.Face, style = Stroke(NARROWED_STROKE, join = StrokeJoin.Round))
            }
        }
        LumiEyes.Closed -> listOf(LumiShapes.closedLeft, LumiShapes.closedRight).forEach { shape ->
            drawPath(shape, LumiShapes.Face, style = line(CLOSED_WIDTH))
        }
    }
}

private fun line(width: Float) = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round)

/** Default size of the mascot. */
val MASCOT_SIZE = 120.dp
private const val VIEWPORT = 256f
private const val INSET = 12.8f
private const val FIGURE_SCALE = 0.9f
private const val BREATH_SWELL = 0.02f
private const val BLINK_DEPTH = 0.9f
private const val CENTER_X = 128f
private const val FEET_Y = 200f
private const val ARM_LEFT_X = 30f
private const val ARM_RIGHT_X = 226f
private const val ARM_Y = 139f
private const val ARM_RX = 12.5f
private const val ARM_RY = 33f
private const val ARM_TILT = 36f
private const val ARM_PIVOT_LEFT_X = 46f
private const val ARM_PIVOT_RIGHT_X = 210f
private const val ARM_PIVOT_Y = 116f
private const val EAR_LEFT_X = 92f
private const val EAR_Y = 72f
private const val REFLECTION_Y = 73f
private const val REFLECTION_RX = 30f
private const val REFLECTION_RY = 4.5f
private const val SCREEN_X = 68f
private const val SCREEN_Y = 81f
private const val SCREEN_W = 121f
private const val SCREEN_H = 89f
private const val SCREEN_RADIUS = 30f
private const val GLINT_WIDTH = 4f
private const val MOUTH_WIDTH = 6f
private const val CHEEK_LEFT_X = 90f
private const val CHEEK_RIGHT_X = 166f
private const val CHEEK_Y = 145f
private const val CHEEK_RADIUS = 4.5f
private const val EYE_LEFT_X = 96f
private const val EYE_RIGHT_X = 146f
private const val EYE_WIDTH = 14f
private const val RING_Y = 150f
private const val RING_RADIUS = 6f
private const val RING_WIDTH = 5f
private const val SPARK_WIDTH = 6f
private const val NARROWED_CENTER_Y = 120f
private const val NARROWED_STROKE = 3f
private const val CLOSED_WIDTH = 7f
