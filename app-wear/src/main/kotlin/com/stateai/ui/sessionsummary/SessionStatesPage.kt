package com.stateai.ui.sessionsummary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.state.DisplayState
import com.stateai.domain.summary.SessionReport
import com.stateai.ui.components.StackedBar
import com.stateai.ui.components.StateIndicator
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.color
import com.stateai.ui.theme.labelRes
import com.stateai.ui.theme.tabular
import kotlin.time.Duration

/** S2: time in each state and how many pauses helped. */
@Composable
fun SessionStatesPage(report: SessionReport) {
    val rows = report.timeByState.filterValues { it.isPositive() }
    Column(
        modifier = Modifier.width(CONTENT_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        StackedBar(
            parts = rows.map { (state, time) -> time.inWholeSeconds.toFloat() to state.color() } +
                (report.noDataTime.inWholeSeconds.toFloat() to StateAiColors.NoData),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(StateAiDimens.SpaceXs))
        rows.forEach { (state, time) -> StateRow(state, time) }
        if (report.noDataTime.isPositive()) StateRow(null, report.noDataTime)
        Spacer(Modifier.height(StateAiDimens.SpaceS))
        PausesSummary(report.helpfulPauses, report.evaluablePauses)
    }
}

@Composable
private fun StateRow(state: DisplayState?, time: Duration) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        StateIndicator(
            state = state,
            label = stringResource(state?.labelRes() ?: R.string.state_no_data),
            size = 12.dp,
            textColor = StateAiColors.Text1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.minutes_value, time.inWholeMinutes),
            fontSize = StateAiDimens.Body,
            color = StateAiColors.Text2,
            style = LocalTextStyle.current.tabular(),
        )
    }
}

@Composable
private fun PausesSummary(helpful: Int, evaluable: Int) {
    if (evaluable == 0) {
        Text(stringResource(R.string.pauses_none), fontSize = StateAiDimens.Body, color = StateAiColors.Text2)
        return
    }
    Text(
        text = stringResource(R.string.pauses_helped_count, helpful, evaluable),
        fontSize = PAUSES_SIZE,
        fontWeight = FontWeight.Medium,
    )
    Text(stringResource(R.string.pauses_helped), fontSize = StateAiDimens.Body, color = StateAiColors.Text2)
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(evaluable) { index -> PauseDot(helped = index < helpful) }
    }
}

@Composable
private fun PauseDot(helped: Boolean) {
    val modifier = Modifier.size(PAUSE_DOT)
    Box(
        if (helped) {
            modifier.background(StateAiColors.Accent, CircleShape)
        } else {
            modifier.border(1.dp, StateAiColors.NoData, CircleShape)
        },
    )
}

private val CONTENT_WIDTH = 145.dp
private val PAUSES_SIZE = 17.sp
private val PAUSE_DOT = 9.dp
