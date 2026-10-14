package com.stateai.ui.summary

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.stateai.domain.learning.DayPart
import com.stateai.domain.learning.Discovery
import com.stateai.domain.learning.DiscoveryConfidence
import com.stateai.ui.common.labelRes
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/**
 * D8: what the person's own ratings say about their best moment of the day. It states a finding only
 * with enough rated sessions, says how sure it is, and otherwise says it does not know yet.
 */
@Composable
fun DiscoveryPage(discovery: Discovery) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
        modifier = Modifier.width(CONTENT_WIDTH),
    ) {
        PageTitle(stringResource(R.string.discovery_title))
        when (discovery) {
            is Discovery.BetterMoment -> BetterMoment(discovery)
            is Discovery.NoClearDifference ->
                Message(R.string.discovery_no_difference, R.string.discovery_no_difference_detail)
            is Discovery.NotEnoughData ->
                Message(R.string.discovery_not_enough, R.string.discovery_not_enough_detail)
        }
        Text(
            text = stringResource(R.string.discovery_basis),
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text3,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BetterMoment(found: Discovery.BetterMoment) {
    Text(
        text = stringResource(found.part.labelRes()),
        fontSize = PART_SIZE,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
    Text(stringResource(found.category.labelRes()), fontSize = StateAiDimens.Body, color = StateAiColors.Accent)
    Detail(stringResource(R.string.discovery_better, found.average, found.otherAverage))
    val confidence = when (found.confidence) {
        DiscoveryConfidence.PROVISIONAL -> R.string.discovery_provisional
        DiscoveryConfidence.MODERATE -> R.string.discovery_moderate
    }
    Text(
        text = stringResource(confidence, found.sessions, found.otherSessions),
        fontSize = StateAiDimens.Label,
        color = StateAiColors.Text2,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Message(@StringRes headline: Int, @StringRes detail: Int) {
    Text(
        text = stringResource(headline),
        fontSize = HEADLINE_SIZE,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
    Detail(stringResource(detail))
}

@Composable
private fun Detail(text: String) {
    Text(text, fontSize = StateAiDimens.Body, color = StateAiColors.Text2, textAlign = TextAlign.Center)
}

@StringRes
private fun DayPart.labelRes(): Int = when (this) {
    DayPart.MORNING -> R.string.discovery_morning
    DayPart.AFTERNOON -> R.string.discovery_afternoon
    DayPart.EVENING -> R.string.discovery_evening
}

private val CONTENT_WIDTH = 150.dp
private val PART_SIZE = 22.sp
private val HEADLINE_SIZE = 18.sp
