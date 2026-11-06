package com.stateai.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.learning.HourlyFocus
import com.stateai.ui.components.Bar
import com.stateai.ui.components.BarChart
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/**
 * D7: share of focus by hour over the last four weeks, the best hour and how many days back it up.
 * While activities are still being learned, the page says so.
 */
@Composable
fun HoursPage(hourly: HourlyFocus, isLearning: Boolean) {
    // On small watches the title and the note would hit the round edge: lower chart, a bit of room above.
    val compact = LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = if (compact) COMPACT_TOP else 0.dp),
    ) {
        PageTitle(stringResource(R.string.hours_title))
        val best = hourly.bestHour
        if (hourly.hours.isEmpty()) {
            MissingData(stringResource(R.string.summary_missing_data), Modifier.width(CHART_WIDTH))
            return@Column
        }
        BarChart(
            bars = hourly.hours.map { Bar(it.share.toFloat(), highlighted = it.hour == best) },
            modifier = Modifier.size(width = CHART_WIDTH, height = if (compact) COMPACT_CHART_HEIGHT else CHART_HEIGHT),
        )
        HourLabels(hourly)
        BestHour(best, hourly.bestHourDays, isLearning)
    }
}

@Composable
private fun HourLabels(hourly: HourlyFocus) {
    Row(Modifier.width(CHART_WIDTH)) {
        hourly.hours.forEach { hour ->
            Text(
                text = if (hour.hour % LABEL_EVERY == 0) hour.hour.toString() else "",
                fontSize = StateAiDimens.Label,
                color = StateAiColors.Text3,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** The best hour and how many days support it, or "still learning" while no hour has enough days. */
@Composable
private fun BestHour(best: Int?, days: Int, isLearning: Boolean) {
    if (best == null) {
        LearningNote()
        return
    }
    Text(stringResource(R.string.hours_best, best, best + 1), fontSize = BEST_SIZE, fontWeight = FontWeight.Medium)
    if (isLearning) {
        LearningNote()
    } else {
        Text(
            text = pluralStringResource(R.plurals.days_support, days, days),
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text2,
        )
    }
}

/** "Learning your patterns…", centered and kept inside the chart width so the round edge does not cut it. */
@Composable
private fun LearningNote() {
    Text(
        text = stringResource(R.string.summary_learning),
        fontSize = StateAiDimens.Label,
        color = StateAiColors.Text2,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(CHART_WIDTH),
    )
}

private val CHART_WIDTH = 160.dp
private val CHART_HEIGHT = 72.dp
private val COMPACT_CHART_HEIGHT = 56.dp
private val COMPACT_TOP = 8.dp
private const val COMPACT_HEIGHT_DP = 210
private val BEST_SIZE = 17.sp
private const val LABEL_EVERY = 4
