package com.stateai.ui.components

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.stateai.ui.theme.StateAiColors

/** Rounded outline icons of the design (36-unit viewport, 4-unit stroke); tinted by `Icon`. */
object StateAiIcons {
    val Pause = outline("M12 8v20M24 8v20")
    val Check = outline("M8 19l7 7 13-14")
    val Plus = outline("M18 8v20M8 18h20")
    val Bars = outline("M10 28V16M18 28V8M26 28v-9")
    val Back = outline("M22 6l-10 12 10 12")

    private fun outline(pathData: String): ImageVector = ImageVector.Builder(
        defaultWidth = 18.dp,
        defaultHeight = 18.dp,
        viewportWidth = VIEWPORT,
        viewportHeight = VIEWPORT,
    ).addPath(
        pathData = addPathNodes(pathData),
        stroke = SolidColor(StateAiColors.Text1),
        strokeLineWidth = STROKE,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

    private const val VIEWPORT = 36f
    private const val STROKE = 4f
}
