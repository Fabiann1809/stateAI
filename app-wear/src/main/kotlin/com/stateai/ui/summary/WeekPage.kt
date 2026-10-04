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
import com.stateai.domain.summary.DayScore
import com.stateai.ui.components.Bar
import com.stateai.ui.components.BarChart
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/** D6: the daily score of the last seven days; days without sessions are a dashed outline. */
@Composable
fun WeekPage(days: List<DayScore>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PageTitle(stringResource(R.string.week_title))
        val today = days.lastOrNull()?.score?.total()?.roundToInt()
        Text(
            text = today?.toString() ?: " ",
            fontSize = TODAY_SIZE,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(CHART_WIDTH),
        )
        BarChart(
            bars = days.mapIndexed { index, day ->
                Bar(day.score?.total()?.let { (it / MAX_SCORE).toFloat() }, highlighted = index == days.lastIndex)
            },
            modifier = Modifier.size(width = CHART_WIDTH, height = CHART_HEIGHT),
        )
        Row(Modifier.width(CHART_WIDTH)) {
            days.forEachIndexed { index, day ->
                Text(
                    text = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, SPANISH),
                    fontSize = StateAiDimens.Label,
                    color = if (index == days.lastIndex) StateAiColors.Text1 else StateAiColors.Text3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private val SPANISH: Locale = Locale.forLanguageTag("es")
private const val MAX_SCORE = 100.0
private val CHART_WIDTH = 152.dp
private val CHART_HEIGHT = 75.dp
private val TODAY_SIZE = 14.sp
