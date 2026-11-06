package com.stateai.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.state.DisplayState
import com.stateai.domain.summary.DaySummary
import com.stateai.domain.summary.SessionReport
import com.stateai.domain.summary.TimelinePoint
import com.stateai.ui.common.toHoursMinutesText
import com.stateai.ui.components.StateIndicator
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.color
import com.stateai.ui.theme.labelRes
import com.stateai.ui.theme.tabular
import java.time.ZoneId
import kotlin.time.Duration

/**
 * D4: the day as a ribbon of colored blocks (one per ten minutes) with the start and end time, and
 * under it the time spent in each state with a bar. Past states have three levels (decision 28).
 */
@Composable
fun DayChartPage(summary: DaySummary?, points: List<TimelinePoint>) {
    val sizes = dayChartSizes()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(sizes.gap),
        modifier = Modifier.width(sizes.width),
    ) {
        PageTitle(stringResource(R.string.summary_today, (summary?.totalTime ?: Duration.ZERO).toHoursMinutesText()))
        if (points.none { it.state != null }) {
            MissingData(stringResource(R.string.summary_missing_data))
            return@Column
        }
        Ribbon(points, sizes.ribbon)
        TimeLabels(points.first(), points.last(), sizes.font)
        val times = timeByState(summary)
        val longest = times.maxOfOrNull { it.second } ?: Duration.ZERO
        Column(verticalArrangement = Arrangement.spacedBy(sizes.rowGap)) {
            times.forEach { (state, time) -> StateRow(state, time, share = time / longest, fontSize = sizes.font) }
        }
        PageTitle(stringResource(R.string.summary_states_estimated))
    }
}

/** Sizes of the day page. Small watches (about 200 dp) get a narrower, lighter page that clears the page dots. */
private data class DayChartSizes(val width: Dp, val gap: Dp, val rowGap: Dp, val ribbon: Dp, val font: TextUnit)

@Composable
private fun dayChartSizes(): DayChartSizes {
    val config = LocalConfiguration.current
    if (config.screenHeightDp >= COMPACT_HEIGHT_DP) {
        val normalGap = StateAiDimens.SpaceXs
        return DayChartSizes(CONTENT_WIDTH, normalGap, StateAiDimens.SpaceS, RIBBON_HEIGHT, StateAiDimens.Label)
    }
    val width = minOf(CONTENT_WIDTH, (config.screenWidthDp - SIDE_MARGINS_DP).dp)
    return DayChartSizes(width, COMPACT_GAP, COMPACT_ROW_GAP, COMPACT_RIBBON_HEIGHT, COMPACT_FONT)
}

@Composable
private fun Ribbon(points: List<TimelinePoint>, height: Dp) {
    Row(Modifier.fillMaxWidth().height(height), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        points.forEach { point ->
            Box(
                Modifier
                    .weight(1f)
                    .height(height)
                    .background(point.state?.color() ?: StateAiColors.NoData, RoundedCornerShape(BLOCK_RADIUS)),
            )
        }
    }
}

@Composable
private fun TimeLabels(first: TimelinePoint, last: TimelinePoint, fontSize: TextUnit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(first, last).forEach { point ->
            val time = point.time.atZone(ZoneId.systemDefault())
            Text(
                text = stringResource(R.string.summary_clock, time.hour, time.minute),
                fontSize = fontSize,
                color = StateAiColors.Text3,
            )
        }
    }
}

@Composable
private fun StateRow(state: DisplayState, time: Duration, share: Double, fontSize: TextUnit) {
    val color = state.color()
    Column(verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StateIndicator(
                state = state,
                label = stringResource(state.labelRes()),
                size = 11.dp,
                fontSize = fontSize,
                textColor = StateAiColors.Text1,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = time.toHoursMinutesText(),
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
                softWrap = false,
                style = LocalTextStyle.current.tabular(),
            )
        }
        Box(Modifier.fillMaxWidth().height(BAR_HEIGHT).background(StateAiColors.Surface, CircleShape)) {
            Box(
                Modifier
                    .fillMaxWidth(share.toFloat().coerceIn(0f, 1f))
                    .height(BAR_HEIGHT)
                    .background(color, CircleShape),
            )
        }
    }
}

/** Total time of each state across today's sessions; the states with no time are left out. */
private fun timeByState(summary: DaySummary?): List<Pair<DisplayState, Duration>> {
    val totals = summary?.segments.orEmpty().map { SessionReport.of(it).timeByState }
    return DisplayState.entries.mapNotNull { state ->
        val total = totals.fold(Duration.ZERO) { sum, byState -> sum + (byState[state] ?: Duration.ZERO) }
        (state to total).takeIf { total.isPositive() }
    }
}

private val CONTENT_WIDTH = 172.dp
private const val COMPACT_HEIGHT_DP = 210
private const val SIDE_MARGINS_DP = 50
private val COMPACT_FONT = 11.sp
private val COMPACT_RIBBON_HEIGHT = 12.dp
private val COMPACT_ROW_GAP = 5.dp
private val COMPACT_GAP = 4.dp
private val RIBBON_HEIGHT = 20.dp
private val BLOCK_RADIUS = 5.dp
private val BAR_HEIGHT = 4.dp
