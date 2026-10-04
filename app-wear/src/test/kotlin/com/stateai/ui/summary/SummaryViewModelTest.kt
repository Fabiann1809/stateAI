package com.stateai.ui.summary

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.cycles.CycleInsight
import com.stateai.domain.cycles.CycleResult
import com.stateai.domain.learning.FocusProfile
import com.stateai.domain.learning.SlotStats
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.state.DisplayState
import com.stateai.domain.summary.DaySummary
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `builds the day points, hourly focus and cycle from its sources`() = runTest {
        val summary = DaySummary(LocalDate.parse("2026-10-04"), listOf(segment()), score = null, bestHour = null)
        val profile = FocusProfile(emptyMap(), mapOf(9 to SlotStats(0.8, 20, 3)), overallFocusShare = 0.8)
        val cycle = CycleInsight(CycleResult.NoClearPattern(CycleResult.Reason.NOT_ENOUGH_DATA), days = 1)
        val viewModel = SummaryViewModel(
            DaySummarySources(
                summary = flowOf(summary),
                recentDays = emptyFlow(),
                energy = emptyFlow(),
                focusProfile = flowOf(profile),
                cycle = flowOf(cycle),
            ),
        )
        viewModel.uiState.launchIn(backgroundScope)

        val state = viewModel.uiState.first { it.cycle != null && it.summary != null && it.hourly.bestHour != null }

        assertEquals(listOf(DisplayState.FOCUSED, DisplayState.NORMAL), state.points.map { it.state })
        assertEquals(9, state.hourly.bestHour)
        assertEquals(cycle, state.cycle)
    }

    private fun segment() = Segment(
        id = SegmentId("s"),
        activity = Activity(ActivityId("a"), ActivityCategory.STUDY, name = null),
        start = Instant.parse("2026-10-04T09:00:00Z"),
        end = Instant.parse("2026-10-04T09:20:00Z"),
        planned = 40.minutes,
        levelTime = LevelDurations(low = 10.minutes, medium = 10.minutes),
        restlessTime = Duration.ZERO,
        pauseSuggestions = 0,
        pauses = emptyList(),
        trace = LevelTrace("L".repeat(10) + "M".repeat(10)),
    )
}
