package com.stateai.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.score.ScoreBreakdown
import com.stateai.ui.components.ScoreGauge
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.tabular
import kotlin.math.roundToInt

/** D1: today's score on the gauge, named under the value, with the estimate and non-medical disclaimer. */
@Composable
fun ScorePage(score: ScoreBreakdown?) {
    val total = score?.total()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val unit = maxWidth
        ScoreGauge(total)
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = unit * VALUE_TOP, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (total == null) {
                MissingData(stringResource(R.string.summary_no_sessions), Modifier.padding(top = 18.dp))
            } else {
                val value = total.roundToInt()
                val description = stringResource(R.string.score_description, value)
                Text(
                    text = value.toString(),
                    fontSize = VALUE_SIZE,
                    fontWeight = FontWeight.Medium,
                    style = LocalTextStyle.current.tabular(),
                    modifier = Modifier.semantics { contentDescription = description },
                )
                Text(
                    text = stringResource(R.string.score_title),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Text2,
                )
            }
        }
        Box(Modifier.align(Alignment.TopCenter).padding(top = unit * DISCLAIMER_TOP, start = 40.dp, end = 40.dp)) {
            Text(
                text = stringResource(R.string.summary_disclaimer),
                fontSize = StateAiDimens.Label,
                color = StateAiColors.Text3,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val VALUE_TOP = 0.2f
private const val DISCLAIMER_TOP = 0.64f
private val VALUE_SIZE = 50.sp
