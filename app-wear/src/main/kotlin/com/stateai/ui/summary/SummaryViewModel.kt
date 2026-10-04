package com.stateai.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.cycles.CycleInsight
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.learning.FocusProfile
import com.stateai.domain.learning.HourlyFocus
import com.stateai.domain.summary.DayScore
import com.stateai.domain.summary.DaySummary
import com.stateai.domain.summary.DayTimeline
import com.stateai.domain.summary.TimelinePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Everything the day summary pages show. Values are null (or empty) until they are known. */
data class DaySummaryUiState(
    val summary: DaySummary? = null,
    val recentDays: List<DayScore> = emptyList(),
    val energy: EnergyBudget? = null,
    val points: List<TimelinePoint> = emptyList(),
    val hourly: HourlyFocus = HourlyFocus.EMPTY,
    val cycle: CycleInsight? = null,
)

/** The flows the day summary needs; cycle detection is slow, so its page fills in when it is ready. */
data class DaySummarySources(
    val summary: Flow<DaySummary>,
    val recentDays: Flow<List<DayScore>>,
    val energy: Flow<EnergyBudget>,
    val focusProfile: Flow<FocusProfile>,
    val cycle: Flow<CycleInsight>,
)

class SummaryViewModel(sources: DaySummarySources, timeline: DayTimeline = DayTimeline()) : ViewModel() {
    val uiState: StateFlow<DaySummaryUiState> = combine(
        sources.summary.nullUntilFirst(),
        sources.recentDays.onStart { emit(emptyList()) },
        sources.energy.nullUntilFirst(),
        sources.focusProfile.map(HourlyFocus::of).onStart { emit(HourlyFocus.EMPTY) },
        sources.cycle.nullUntilFirst(),
    ) { summary, recentDays, energy, hourly, cycle ->
        DaySummaryUiState(
            summary = summary,
            recentDays = recentDays,
            energy = energy,
            points = summary?.segments?.let(timeline::points).orEmpty(),
            hourly = hourly,
            cycle = cycle,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DaySummaryUiState())

    private fun <T : Any> Flow<T>.nullUntilFirst(): Flow<T?> = map<T, T?> { it }.onStart { emit(null) }
}
