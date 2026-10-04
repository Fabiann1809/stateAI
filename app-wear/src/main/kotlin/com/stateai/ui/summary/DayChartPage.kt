package com.stateai.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.state.DisplayState
import com.stateai.domain.summary.DaySummary
import com.stateai.domain.summary.TimelinePoint
import com.stateai.ui.common.toHoursMinutesText
import com.stateai.ui.components.ChartPoint
import com.stateai.ui.components.DotLineChart
import com.stateai.ui.components.StateIndicator
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.labelRes
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration.Companion.ZERO

/** D4: today's states as points through the day, with a legend. Past states have three levels (decision 28). */
@Composable
fun DayChartPage(summary: DaySummary?, points: List<TimelinePoint>) {
    val known = points.filter { it.state != null }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        PageTitle(stringResource(R.string.summary_today, (summary?.totalTime ?: ZERO).toHoursMinutesText()))
        if (known.isEmpty()) {
            MissingData(stringResource(R.string.summary_missing_data), Modifier.width(150.dp))
            return@Column
        }
        val focus = summary?.segments?.fold(ZERO) { total, segment -> total + segment.levelTime.low } ?: ZERO
        Text(
            text = stringResource(R.string.summary_focus_time, focus.toHoursMinutesText()),
            fontSize = HEADER_SIZE,
            fontWeight = FontWeight.Medium,
        )
        val axis = HourAxis.of(known)
        DotLineChart(
            points = known.map { ChartPoint(axis.fraction(it), requireNotNull(it.state)) },
            gridX = axis.labels.indices.map { it / (axis.labels.size - 1f) },
            modifier = Modifier.size(width = CHART_WIDTH, height = CHART_HEIGHT),
        )
        HourLabels(axis.labels)
        Legend()
        Text(
            text = stringResource(R.string.summary_states_estimated),
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text3,
        )
    }
}

@Composable
private fun HourLabels(hours: List<Int>) {
    Row(Modifier.width(CHART_WIDTH + LABEL_WIDTH), horizontalArrangement = Arrangement.SpaceBetween) {
        hours.forEach { hour ->
            Text(
                text = stringResource(R.string.summary_hour, hour),
                fontSize = StateAiDimens.Label,
                color = StateAiColors.Text3,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(LABEL_WIDTH),
            )
        }
    }
}

/** Two rows, like the design's 2 x 2 legend (past states have three levels). */
@Composable
private fun Legend() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceM)) {
            LegendItem(DisplayState.FOCUSED)
            LegendItem(DisplayState.NORMAL)
        }
        LegendItem(DisplayState.OVERLOADED)
    }
}

@Composable
private fun LegendItem(state: DisplayState) {
    StateIndicator(
        state = state,
        label = stringResource(state.labelRes()),
        size = 9.dp,
        fontSize = StateAiDimens.Label,
        textColor = StateAiColors.Text2,
    )
}

/** Whole hours around today's points, with three evenly spaced labels. */
private class HourAxis(private val startMinute: Double, private val endMinute: Double, val labels: List<Int>) {
    fun fraction(point: TimelinePoint): Float =
        ((minuteOfDay(point) - startMinute) / (endMinute - startMinute)).toFloat().coerceIn(0f, 1f)

    companion object {
        fun of(points: List<TimelinePoint>): HourAxis {
            val minutes = points.map(::minuteOfDay)
            var start = floor(minutes.min() / MINUTES_PER_HOUR).toInt()
            var end = ceil(minutes.max() / MINUTES_PER_HOUR).toInt()
            if (end - start < MIN_HOURS) end = start + MIN_HOURS
            if ((end - start) % 2 != 0) end += 1
            if (end > LAST_HOUR) {
                start -= end - LAST_HOUR
                end = LAST_HOUR
            }
            return HourAxis(start * MINUTES_PER_HOUR, end * MINUTES_PER_HOUR, listOf(start, (start + end) / 2, end))
        }

        private fun minuteOfDay(point: TimelinePoint): Double {
            val time = point.time.atZone(ZoneId.systemDefault())
            return time.hour * MINUTES_PER_HOUR + time.minute
        }

        private const val MINUTES_PER_HOUR = 60.0
        private const val MIN_HOURS = 2
        private const val LAST_HOUR = 24
    }
}

private val CHART_WIDTH = 150.dp
private val CHART_HEIGHT = 62.dp
private val LABEL_WIDTH = 34.dp
private val HEADER_SIZE = 14.sp
