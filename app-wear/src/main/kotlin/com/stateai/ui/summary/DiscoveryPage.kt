package com.stateai.ui.summary

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.learning.DayPart
import com.stateai.domain.learning.Discovery
import com.stateai.domain.learning.DiscoveryConfidence
import com.stateai.ui.common.labelRes
import com.stateai.ui.mascot.Mascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.tabular

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
        Badge()
        when (discovery) {
            is Discovery.BetterMoment -> BetterMoment(discovery)
            is Discovery.NoClearDifference ->
                Message(R.string.discovery_no_difference, R.string.discovery_no_difference_detail)
            is Discovery.NotEnoughData ->
                Message(R.string.discovery_not_enough, R.string.discovery_not_enough_detail)
        }
    }
}

/** The page title as a turquoise-to-violet capsule. */
@Composable
private fun Badge() {
    Box(
        Modifier
            .shadow(
                BADGE_GLOW,
                CircleShape,
                ambientColor = StateAiColors.AccentGlow,
                spotColor = StateAiColors.AccentGlow,
            )
            .background(StateAiColors.AccentGradient, CircleShape)
            .padding(horizontal = BADGE_PADDING, vertical = BADGE_VERTICAL_PADDING),
    ) {
        Text(
            text = stringResource(R.string.discovery_title),
            fontSize = StateAiDimens.Label,
            fontWeight = FontWeight.SemiBold,
            color = StateAiColors.OnAccent,
        )
    }
}

@Composable
private fun BetterMoment(found: Discovery.BetterMoment) {
    Text(
        text = stringResource(R.string.discovery_headline, stringResource(found.category.labelRes())),
        fontSize = HEADLINE_SIZE,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 2,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceL)) {
        Score(found.average, stringResource(found.part.labelRes()), better = true)
        Score(found.otherAverage, stringResource(R.string.discovery_rest), better = false)
    }
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

/** An average rating in a circle: the better moment is filled with the gradient, the rest is glass. */
@Composable
private fun Score(average: Double, label: String, better: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
    ) {
        val circle = Modifier.size(SCORE_SIZE)
        Box(
            modifier = if (better) {
                circle
                    .shadow(
                        SCORE_GLOW,
                        CircleShape,
                        ambientColor = StateAiColors.AccentGlow,
                        spotColor = StateAiColors.AccentGlow,
                    )
                    .background(StateAiColors.AccentGradient, CircleShape)
            } else {
                circle
                    .background(StateAiColors.Surface, CircleShape)
                    .border(BorderStroke(1.dp, StateAiColors.Outline), CircleShape)
            },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.discovery_average, average),
                fontSize = SCORE_TEXT,
                fontWeight = if (better) FontWeight.Bold else FontWeight.SemiBold,
                color = if (better) StateAiColors.OnAccent else StateAiColors.Text2,
                style = LocalTextStyle.current.tabular(),
            )
        }
        Text(
            text = label,
            fontSize = StateAiDimens.Label,
            fontWeight = if (better) FontWeight.SemiBold else FontWeight.Normal,
            color = if (better) StateAiColors.Text1 else StateAiColors.Text2,
        )
    }
}

@Composable
private fun Message(@StringRes headline: Int, @StringRes detail: Int) {
    Mascot(MascotExpression.THINKING, size = MASCOT_SIZE)
    Text(
        text = stringResource(headline),
        fontSize = MESSAGE_SIZE,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(detail),
        fontSize = StateAiDimens.Label,
        color = StateAiColors.Text2,
        textAlign = TextAlign.Center,
    )
}

@StringRes
private fun DayPart.labelRes(): Int = when (this) {
    DayPart.MORNING -> R.string.discovery_morning
    DayPart.AFTERNOON -> R.string.discovery_afternoon
    DayPart.EVENING -> R.string.discovery_evening
}

private val CONTENT_WIDTH = 150.dp
private val HEADLINE_SIZE = 13.sp
private val MESSAGE_SIZE = 17.sp
private val SCORE_TEXT = 18.sp
private val SCORE_SIZE: Dp = 46.dp
private val SCORE_GLOW: Dp = 12.dp
private val BADGE_GLOW: Dp = 8.dp
private val BADGE_PADDING: Dp = 9.dp
private val BADGE_VERTICAL_PADDING: Dp = 4.dp
private val MASCOT_SIZE: Dp = 52.dp
