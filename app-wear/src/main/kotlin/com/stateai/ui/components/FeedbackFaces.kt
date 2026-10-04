package com.stateai.ui.components

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.stateai.domain.segment.Feedback
import com.stateai.ui.theme.StateAiColors

/** Soft outline faces for the "how did you feel?" answers (48-unit viewport). */
object FeedbackFaces {
    private val good = face("M16 29q8 8 16 0")
    private val okay = face("M17 31h14")
    private val bad = face("M16 33q8-7 16 0")

    fun of(feedback: Feedback): ImageVector = when (feedback) {
        Feedback.GOOD -> good
        Feedback.OKAY -> okay
        Feedback.BAD -> bad
    }

    private fun face(mouth: String): ImageVector = ImageVector.Builder(
        defaultWidth = 30.dp,
        defaultHeight = 30.dp,
        viewportWidth = VIEWPORT,
        viewportHeight = VIEWPORT,
    ).apply {
        stroke("M24 5a19 19 0 1 0 0.01 0Z", OUTLINE)
        stroke("M17 20v0.5M31 20v0.5", EYES)
        stroke(mouth, OUTLINE)
    }.build()

    private fun ImageVector.Builder.stroke(pathData: String, width: Float) = addPath(
        pathData = addPathNodes(pathData),
        stroke = SolidColor(StateAiColors.Text1),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
    )

    private const val VIEWPORT = 48f
    private const val OUTLINE = 3f
    private const val EYES = 4f
}
