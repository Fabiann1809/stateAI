package com.stateai.ui.summary

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import com.stateai.R
import com.stateai.domain.score.ScoreBreakdown
import com.stateai.ui.components.FactorCard
import com.stateai.ui.components.StateAiIcons

/** One score component: how to label it and how to read it from a breakdown. */
private data class Factor(@StringRes val name: Int, val icon: ImageVector, val value: (ScoreBreakdown) -> Double)

private val FACTORS = listOf(
    Factor(R.string.summary_focus, StateAiIcons.Focus) { it.focus },
    Factor(R.string.summary_recovery, StateAiIcons.Recovery) { it.recovery },
    Factor(R.string.summary_load, StateAiIcons.Load) { it.sustainableLoad },
    Factor(R.string.summary_consistency, StateAiIcons.Consistency) { it.consistency },
)

/** D2: the four components of the score, each with its change since yesterday when there is one. */
@Composable
fun FactorsPage(score: ScoreBreakdown?, change: ScoreBreakdown?) {
    val listState = rememberScalingLazyListState()
    ScalingLazyColumn(state = listState) {
        item { ListHeader { PageTitle(stringResource(R.string.summary_factors)) } }
        if (score == null) {
            item { MissingData(stringResource(R.string.summary_no_sessions)) }
        } else {
            items(FACTORS) { factor ->
                FactorCard(
                    icon = factor.icon,
                    name = stringResource(factor.name),
                    value = factor.value(score),
                    change = change?.let(factor.value),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}
