package com.stateai.domain.summary

import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.DisplayState
import com.stateai.domain.state.StateEstimate
import com.stateai.domain.testing.testSegment
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SessionReportTest {
    @Test
    fun `each minute window becomes a display state or no data`() {
        val report = SessionReport.of(testSegment(duration = 12.minutes, trace = LevelTrace("-LLMH")))

        assertEquals(
            listOf(null, DisplayState.FOCUSED, DisplayState.FOCUSED, DisplayState.NORMAL, DisplayState.OVERLOADED),
            report.minutes,
        )
    }

    @Test
    fun `time per state comes from the recorded level durations`() {
        val levels = LevelDurations(low = 20.minutes, medium = 9.minutes, high = 5.minutes, unknown = 4.minutes)
        val report = SessionReport.of(testSegment(duration = 38.minutes, levelTime = levels))

        assertEquals(20.minutes, report.timeByState[DisplayState.FOCUSED])
        assertEquals(9.minutes, report.timeByState[DisplayState.NORMAL])
        assertEquals(5.minutes, report.timeByState[DisplayState.OVERLOADED])
        assertEquals(4.minutes, report.noDataTime)
    }

    @Test
    fun `only pauses with estimates on both sides are judged`() {
        val pauses = listOf(
            pause(ActivationLevel.HIGH, ActivationLevel.MEDIUM),
            pause(ActivationLevel.MEDIUM, ActivationLevel.MEDIUM),
            pause(ActivationLevel.HIGH, ActivationLevel.LOW),
            PauseRecord(Instant.EPOCH, Instant.EPOCH, before = null, after = estimate(ActivationLevel.LOW)),
        )
        val report = SessionReport.of(testSegment().copy(pauses = pauses))

        assertEquals(3, report.evaluablePauses)
        assertEquals(2, report.helpfulPauses)
    }

    @Test
    fun `a short session does not count for learning`() {
        assertTrue(SessionReport.of(testSegment(duration = 30.minutes)).counted)
        assertFalse(SessionReport.of(testSegment(duration = 6.minutes)).counted)
    }

    private fun pause(before: ActivationLevel, after: ActivationLevel) =
        PauseRecord(Instant.EPOCH, Instant.EPOCH, estimate(before), estimate(after))

    private fun estimate(level: ActivationLevel) = StateEstimate(level, restless = false)
}
