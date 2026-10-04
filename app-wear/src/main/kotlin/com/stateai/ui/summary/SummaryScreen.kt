package com.stateai.ui.summary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.ScreenScaffold
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.summary.changeSinceYesterday
import com.stateai.ui.components.EnergyOverview
import com.stateai.ui.components.PageIndicator
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
                    ),
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(state)
}

/** Day summary as a carousel, one idea per page: how it went, why, and when it repeats. */
@Composable
fun SummaryScreen(state: DaySummaryUiState) {
    val pagerState = rememberPagerState(pageCount = { PAGE_CONTENT.size })
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SummaryPage(page, state) }
            }
            PageIndicator(
                count = PAGE_CONTENT.size,
                current = pagerState.currentPage,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun SummaryPage(page: Int, state: DaySummaryUiState) {
    PAGE_CONTENT[page](state)
}

/** D1 to D7, from "how did it go" to "when does it repeat". */
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
)
