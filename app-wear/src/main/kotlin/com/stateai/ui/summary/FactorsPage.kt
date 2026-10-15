package com.stateai.ui.summary

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stateai.R
import com.stateai.domain.score.ScoreBreakdown
import com.stateai.ui.components.FactorCard
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiDimens

/** One score component: how to label it and how to read it from a breakdown. */
private data class Factor(@StringRes val name: Int, val icon: ImageVector, val value: (ScoreBreakdown) -> Double)

private val FACTORS = listOf(
    Factor(R.string.summary_focus, StateAiIcons.Focus) { it.focus },
    Factor(R.string.summary_recovery, StateAiIcons.Recovery) { it.recovery },
    Factor(R.string.summary_load, StateAiIcons.Load) { it.sustainableLoad },
    Factor(R.string.summary_consistency, StateAiIcons.Consistency) { it.consistency },
)

/**
 * D2: the four components of the score, each with its change since yesterday when there is one. The
 * cards are taller than the screen, so the page scrolls on its own before moving on to the next one.
 */
@Composable
fun FactorsPage(score: ScoreBreakdown?, change: ScoreBreakdown?) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = TOP_PADDING, bottom = BOTTOM_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
    ) {
        PageTitle(stringResource(R.string.summary_factors))
        if (score == null) {
            MissingData(stringResource(R.string.summary_no_sessions))
        } else {
            FACTORS.forEach { factor ->
                FactorCard(
                    icon = factor.icon,
                    name = stringResource(factor.name),
                    value = factor.value(score),
                    change = change?.let(factor.value),
                    modifier = Modifier.padding(horizontal = CARD_PADDING),
                )
            }
        }
    }
}

private val TOP_PADDING = 24.dp
private val BOTTOM_PADDING = 24.dp
private val CARD_PADDING = 20.dp
