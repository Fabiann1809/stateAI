package com.stateai.domain.session

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.baseline.BaselineKeeper
import com.stateai.domain.baseline.BaselineRepository
import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.sensing.SensorSample
import com.stateai.domain.sensing.SensorSource
import com.stateai.domain.state.ActivationLevel
import java.time.Instant
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SessionMonitorTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")
    private val session = ActiveSession(
        Activity(ActivityId("study"), ActivityCategory.STUDY, name = null),
        DefaultCategoryProfiles.STUDY,
        startedAt = start,
    )
    private val played = mutableListOf<HapticEvent>()
    private val outcomes = mutableListOf<Pair<Suggestion, SuggestionOutcome>>()
    private val suggestions = SessionSuggestions { suggestion, outcome -> outcomes += suggestion to outcome }
    private var allowed = true
    private val recorder = SegmentRecorder { SegmentId("segment") }.apply { start(session) }
    private val repository = object : BaselineRepository {
        var stored: UserBaseline? = null

        override suspend fun load() = stored

        override suspend fun save(baseline: UserBaseline) {
            stored = baseline
        }
    }

    @Test
    fun `calibrates first and then estimates the level`() = runTest {
        val monitor = monitorWith(heartRateAt = { 65.0 }, seconds = 600)

        monitor.run(session)

        assertNotNull(repository.stored)
        assertEquals(ActivationLevel.LOW, (monitor.status.value as MonitorStatus.Estimating).estimate.level)
    }

    @Test
    fun `sustained high activation triggers the overload alert`() = runTest {
        repository.stored = UserBaseline(65.0, 1.0, start)
        val monitor = monitorWith(heartRateAt = { second -> if (second < 180) 65.0 else 85.0 }, seconds = 900)

        monitor.run(session)

        assertEquals(ActivationLevel.HIGH, (monitor.status.value as MonitorStatus.Estimating).estimate.level)
        assertTrue(HapticEvent.OVERLOAD_ALERT in played, "$played")
        // The session ended with the suggestion unanswered, so it was reported as ignored.
        assertEquals(listOf(SuggestionOutcome.IGNORED), outcomes.map { it.second })
        assertEquals(SuggestionReason.OVERLOAD, outcomes.single().first.reason)
        assertEquals(PauseKind.BREATHE, outcomes.single().first.reason.pause)
        assertNull(suggestions.current.value)
    }

    @Test
    fun `a suggestion held back by the gate neither vibrates nor shows`() = runTest {
        repository.stored = UserBaseline(65.0, 1.0, start)
        allowed = false
        val monitor = monitorWith(heartRateAt = { second -> if (second < 180) 65.0 else 85.0 }, seconds = 900)

        monitor.run(session)

        assertTrue(played.isEmpty(), "$played")
        assertTrue(outcomes.isEmpty())
    }

    @Test
    fun `records time per level in the segment`() = runTest {
        repository.stored = UserBaseline(65.0, 1.0, start)
        val monitor = monitorWith(heartRateAt = { 65.0 }, seconds = 600)

        monitor.run(session)

        val segment = recorder.finish(start.plusSeconds(600))!!
        assertTrue(segment.levelTime.low.inWholeMinutes >= 8, "${segment.levelTime}")
    }

    private fun monitorWith(heartRateAt: (Int) -> Double, seconds: Int): SessionMonitor {
        val samples = (0 until seconds).map { SensorSample(start.plusSeconds(it.toLong()), heartRateAt(it), 0.05) }
        return SessionMonitor(
            sensorSource = SensorSource { samples.asFlow() },
            baselineKeeper = BaselineKeeper(repository),
            player = { played += it },
            recorder = recorder,
            suggestions = suggestions,
            gate = { allowed },
        )
    }
}
