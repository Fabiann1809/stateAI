package com.stateai.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.score.ScoreBreakdown
import com.stateai.domain.summary.DaySummary
import com.stateai.ui.common.EnergyText
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.flowOf

@Composable
fun SummaryRoute() {
    val container = appContainer()
    val viewModel: SummaryViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val insights = container.insights
                SummaryViewModel(insights.observeDaySummary, insights.observeEnergy(flowOf(container.clock.instant())))
            }
        },
    )
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val energy by viewModel.energy.collectAsStateWithLifecycle()
    SummaryScreen(summary, energy)
}

@Composable
fun SummaryScreen(summary: DaySummary?, energy: EnergyBudget?) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.summary_title)) } }
            energy?.let { item { EnergyText(it) } }
            val score = summary?.score
            if (summary == null || score == null) {
                item { Text(stringResource(R.string.summary_empty), textAlign = TextAlign.Center) }
            } else {
                scoreItems(score)
                dayItems(summary)
            }
        }
    }
}

private fun ScalingLazyListScope.scoreItems(score: ScoreBreakdown) {
    item {
        Text(
            text = stringResource(R.string.summary_total, score.total().roundToInt()),
            style = MaterialTheme.typography.displaySmall,
        )
    }
    item { ComponentText(R.string.summary_focus, score.focus) }
    item { ComponentText(R.string.summary_recovery, score.recovery) }
    item { ComponentText(R.string.summary_load, score.sustainableLoad) }
    item { ComponentText(R.string.summary_consistency, score.consistency) }
}

private fun ScalingLazyListScope.dayItems(summary: DaySummary) {
    item { Text(stringResource(R.string.summary_time, summary.totalTime.inWholeMinutes)) }
    summary.bestHour?.let { hour -> item { Text(stringResource(R.string.summary_best_hour, hour)) } }
    item {
        Text(
            text = stringResource(R.string.summary_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ComponentText(labelRes: Int, value: Double) {
    Text(stringResource(R.string.summary_component, stringResource(labelRes), value.roundToInt()))
}
