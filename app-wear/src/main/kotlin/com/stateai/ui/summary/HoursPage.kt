package com.stateai.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PageTitle(stringResource(R.string.hours_title))
        val best = hourly.bestHour
        if (hourly.hours.isEmpty() || best == null) {
            MissingData(stringResource(R.string.summary_missing_data), Modifier.width(CHART_WIDTH))
            return@Column
        }
        BarChart(
            bars = hourly.hours.map { Bar(it.share.toFloat(), highlighted = it.hour == best) },
            modifier = Modifier.size(width = CHART_WIDTH, height = CHART_HEIGHT),
            dimAlpha = DIM_ALPHA,
        )
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
        Text(stringResource(R.string.hours_best, best, best + 1), fontSize = BEST_SIZE, fontWeight = FontWeight.Medium)
        Text(
            text = if (isLearning) {
                stringResource(R.string.summary_learning)
            } else {
                pluralStringResource(R.plurals.days_support, hourly.bestHourDays, hourly.bestHourDays)
            },
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text2,
        )
    }
}

private val CHART_WIDTH = 160.dp
private val CHART_HEIGHT = 72.dp
private val BEST_SIZE = 17.sp
private const val LABEL_EVERY = 4
private const val DIM_ALPHA = 0.4f
