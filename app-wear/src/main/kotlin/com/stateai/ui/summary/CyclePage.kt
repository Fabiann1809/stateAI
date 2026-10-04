package com.stateai.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.cycles.CycleInsight
import com.stateai.domain.cycles.CycleResult
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * D5: the person's focus cycle. It is only stated when detection is confident; otherwise the page
 * says so honestly ("no clear pattern" is an expected answer, SPEC 7.4).
 */
@Composable
fun CyclePage(insight: CycleInsight?) {
    val detected = insight?.result as? CycleResult.Detected
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
        modifier = Modifier.width(CONTENT_WIDTH),
    ) {
        PageTitle(stringResource(R.string.cycle_title))
        CycleWave(detected != null)
        if (detected != null) {
            val minutes = detected.periodMinutes.roundToInt()
            Text(stringResource(R.string.cycle_value, minutes), fontSize = VALUE_SIZE, fontWeight = FontWeight.Medium)
            Detail(stringResource(R.string.cycle_detected, minutes))
        } else {
            Text(
                text = stringResource(R.string.cycle_none),
                fontSize = NONE_SIZE,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Detail(stringResource(if (insight.isLearning()) R.string.summary_learning else R.string.cycle_none_detail))
        }
        insight?.takeIf { it.days > 0 }?.let {
            Text(
                text = pluralStringResource(R.plurals.days_of_data, it.days, it.days),
                fontSize = StateAiDimens.Label,
                color = StateAiColors.Text3,
            )
        }
    }
}

private fun CycleInsight?.isLearning(): Boolean =
    this == null || result == CycleResult.NoClearPattern(CycleResult.Reason.NOT_ENOUGH_DATA)

@Composable
private fun Detail(text: String) {
    Text(text, fontSize = StateAiDimens.Body, color = StateAiColors.Text2, textAlign = TextAlign.Center)
}

/** A calm sine wave when there is a cycle; a dashed flat line when there is not. */
@Composable
private fun CycleWave(detected: Boolean) {
    Canvas(Modifier.size(width = CONTENT_WIDTH, height = WAVE_HEIGHT)) {
        val middle = size.height / 2
        if (!detected) {
            drawLine(
                color = StateAiColors.NoData,
                start = Offset(0f, middle),
                end = Offset(size.width, middle),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 6.dp.toPx())),
            )
            return@Canvas
        }
        val amplitude = middle - 2.dp.toPx()
        val path = Path().apply {
            moveTo(0f, middle)
            var x = 0f
            while (x <= size.width) {
                lineTo(x, middle + amplitude * sin(2 * PI * x / (size.width / WAVES)).toFloat())
                x += 2f
            }
        }
        drawPath(path, StateAiColors.Accent, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

private val CONTENT_WIDTH = 150.dp
private val WAVE_HEIGHT = 28.dp
private val VALUE_SIZE = 38.sp
private val NONE_SIZE = 20.sp
private const val WAVES = 3
