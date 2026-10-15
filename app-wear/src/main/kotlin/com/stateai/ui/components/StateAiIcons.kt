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
    val Down = outline("M9 14l9 9 9-9")
    val List = outline("M9 11h18M9 18h18M9 25h18")

    val Focus = thin("M12 21a9 9 0 1 0 0-18a9 9 0 0 0 0 18ZM12 15a3 3 0 1 0 0-6a3 3 0 0 0 0 6Z")
    val Recovery = thin("M3 12q3-6 6 0t6 0t6 0")
    val Load = thin("M4 8h13a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2ZM22 11v2")
    val Consistency = thin("M4 4v6h6M20 20v-6h-6M5.5 15a8 8 0 0 0 13 3M18.5 9a8 8 0 0 0-13-3")

    val DeepWork = thin("M12 3a9 9 0 1 0 0 18a9 9 0 0 0 0-18ZM12 8a4 4 0 1 0 0 8a4 4 0 0 0 0-8Z")
    val Study = thin("M3 8l9-4 9 4-9 4-9-4ZM7 10v5c0 1.8 2.2 3 5 3s5-1.2 5-3v-5")
    val Reading = thin("M3 5h6a3 3 0 0 1 3 3v12a2 2 0 0 0-2-2H3ZM21 5h-6a3 3 0 0 0-3 3v12a2 2 0 0 1 2-2h7Z")
    val Collab =
        thin("M9 11a3 3 0 1 0 0-6a3 3 0 0 0 0 6ZM3 20a6 6 0 0 1 12 0M17 11a2.5 2.5 0 1 0 0-5M21 20a5 5 0 0 0-4-5")
    val Other = thin("M12 4v4M12 16v4M4 12h4M16 12h4")

    private fun outline(pathData: String): ImageVector = icon(pathData, VIEWPORT, STROKE)

    /** 24-unit icons with a 2-unit stroke (factor cards). */
    private fun thin(pathData: String): ImageVector = icon(pathData, THIN_VIEWPORT, THIN_STROKE)

    private fun icon(pathData: String, viewport: Float, stroke: Float): ImageVector = ImageVector.Builder(
        defaultWidth = 18.dp,
        defaultHeight = 18.dp,
        viewportWidth = viewport,
        viewportHeight = viewport,
    ).addPath(
        pathData = addPathNodes(pathData),
        stroke = SolidColor(StateAiColors.Text1),
        strokeLineWidth = stroke,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

    private const val VIEWPORT = 36f
    private const val STROKE = 4f
    private const val THIN_VIEWPORT = 24f
    private const val THIN_STROKE = 2f
}
