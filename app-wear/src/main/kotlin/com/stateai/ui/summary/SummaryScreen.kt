package com.stateai.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.ScreenScaffold
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.summary.changeSinceYesterday
import com.stateai.ui.components.EnergyOverview
import com.stateai.ui.components.VerticalPages
import kotlinx.coroutines.flow.flowOf

@Composable
fun SummaryRoute() {
    val container = appContainer()
    val viewModel: SummaryViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val insights = container.insights
                SummaryViewModel(
                    DaySummarySources(
                        summary = insights.observeDaySummary(),
                        recentDays = insights.observeRecentDays(),
                        energy = insights.observeEnergy(flowOf(container.clock.instant())),
                        focusProfile = insights.observeFocusProfile(),
                        cycle = insights.observeCycle(),
                        discovery = insights.observeDiscovery(),
                    ),
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(state)
}

/** Day summary as pages that scroll down, one idea per page: how it went, why, and when it repeats. */
@Composable
fun SummaryScreen(state: DaySummaryUiState) {
    ScreenScaffold(timeText = {}) {
        VerticalPages(count = PAGE_CONTENT.size) { page -> PAGE_CONTENT[page](state) }
    }
}

/** D1 to D8, from "how did it go" to "when does it repeat". */
private val PAGE_CONTENT: List<@Composable (DaySummaryUiState) -> Unit> = listOf(
    { ScorePage(it.summary?.score) },
    { FactorsPage(it.summary?.score, it.recentDays.changeSinceYesterday()) },
    { state ->
        state.energy?.let { EnergyOverview(it, displayState = null) }
            ?: MissingData(stringResource(R.string.summary_missing_data))
    },
    { DayChartPage(it.summary, it.points) },
    { CyclePage(it.cycle) },
    { WeekPage(it.recentDays) },
    { HoursPage(it.hourly, isLearning = it.summary?.isLearning == true) },
    { DiscoveryPage(it.discovery) },
)
