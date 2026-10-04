package com.stateai.ui.sessionsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.summary.SessionReport
import com.stateai.ui.common.title
import com.stateai.ui.components.RoundIconButton
import com.stateai.ui.components.SegmentedRing
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.tabular

/** S1: the session as a ring of minutes, with the real and planned duration in the center. */
@Composable
fun SessionTimelinePage(report: SessionReport, onDone: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SegmentedRing(report.minutes, plannedMinutes = report.planned.inWholeMinutes.toInt())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.padding(horizontal = CENTER_PADDING),
        ) {
            Text(report.segment.activity.title(), fontSize = StateAiDimens.Label, color = StateAiColors.Text3)
            Text(
                text = stringResource(R.string.minutes_value, report.duration.inWholeMinutes),
                fontSize = DURATION_SIZE,
                fontWeight = FontWeight.Medium,
                style = LocalTextStyle.current.tabular(),
            )
            Text(
                text = stringResource(R.string.session_target, report.planned.inWholeMinutes),
                fontSize = StateAiDimens.Body,
                color = StateAiColors.Text2,
            )
            if (!report.counted) {
                Text(
                    text = stringResource(R.string.session_not_counted),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Text3,
                    textAlign = TextAlign.Center,
                )
            }
            RoundIconButton(
                icon = StateAiIcons.Check,
                contentDescription = stringResource(R.string.summary_done),
                onClick = onDone,
                tint = StateAiColors.Accent,
            )
        }
    }
}

private val DURATION_SIZE = 32.sp
private val CENTER_PADDING = 44.dp
