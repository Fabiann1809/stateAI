package com.stateai.ui.mascot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The mascot in one [expression]. The three layers share the same 256-unit viewport and stack
 * exactly; [tipRotation] turns only the tip around its base (the pivot of the design, 128, 100).
 */
@Composable
fun Mascot(
    expression: MascotExpression,
    modifier: Modifier = Modifier,
    size: Dp = MASCOT_SIZE,
    tipRotation: Float = 0f,
    contentDescription: String? = null,
) {
    Box(modifier.size(size)) {
        Layer(expression.body, contentDescription)
        Layer(
            expression.tip,
            description = null,
            modifier = Modifier.graphicsLayer {
                rotationZ = tipRotation
                transformOrigin = TIP_PIVOT
            },
        )
        Layer(expression.face, description = null)
    }
}

@Composable
private fun Layer(drawable: Int, description: String?, modifier: Modifier = Modifier) {
    Image(painterResource(drawable), contentDescription = description, modifier = modifier.fillMaxSize())
}

/** Default mascot size: the drawables are 120 dp. */
val MASCOT_SIZE = 120.dp
private const val VIEWPORT = 256f
private val TIP_PIVOT = TransformOrigin(pivotFractionX = 128f / VIEWPORT, pivotFractionY = 100f / VIEWPORT)
