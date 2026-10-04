package com.stateai.domain.score

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SegmentScorerTest {
    private val scorer = SegmentScorer()
    private val start = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `all focus for the planned block scores 100`() {
        val breakdown = scorer.score(segment(levels = LevelDurations(low = 40.minutes)))

        assertEquals(ScoreBreakdown(100.0, 100.0, 100.0, 100.0), breakdown)
        assertEquals(100.0, breakdown.total(), 1e-9)
    }

    @Test
    fun `all high activation without pauses scores only consistency`() {
        val breakdown = scorer.score(segment(levels = LevelDurations(high = 40.minutes), suggestions = 2))

        assertEquals(ScoreBreakdown(0.0, 0.0, 0.0, 100.0), breakdown)
        assertEquals(15.0, breakdown.total(), 1e-9)
    }

    @Test
    fun `recovery counts pauses that improved the state`() {
        val helped = pause(ActivationLevel.HIGH, ActivationLevel.LOW)
        val useless = pause(ActivationLevel.MEDIUM, ActivationLevel.MEDIUM)
        val segment = segment(LevelDurations(low = 30.minutes, high = 10.minutes), 2, listOf(helped, useless))

        assertEquals(50.0, scorer.score(segment).recovery, 1e-9)
    }

    @Test
    fun `pauses without estimates on both sides are not judged`() {
        val unknown = PauseRecord(start, start.plusSeconds(60), before = null, after = null)
        val segment = segment(LevelDurations(low = 40.minutes), pauses = listOf(unknown))

        assertEquals(100.0, scorer.score(segment).recovery, 1e-9)
    }

    @Test
    fun `restless time lowers the sustainable load component`() {
        val segment = segment(LevelDurations(low = 40.minutes), restless = 10.minutes)

        assertEquals(75.0, scorer.score(segment).sustainableLoad, 1e-9)
    }

    @Test
    fun `stopping halfway halves consistency`() {
        val segment = segment(LevelDurations(low = 20.minutes), duration = 20.minutes)

        assertEquals(50.0, scorer.score(segment).consistency, 1e-9)
    }

    @Test
    fun `calibration time does not count against focus`() {
        val segment = segment(LevelDurations(low = 30.minutes, unknown = 10.minutes))

        assertEquals(100.0, scorer.score(segment).focus, 1e-9)
    }

    private fun pause(before: ActivationLevel, after: ActivationLevel) =
        PauseRecord(start, start.plusSeconds(120), StateEstimate(before, false), StateEstimate(after, false))

    private fun segment(
        levels: LevelDurations,
        suggestions: Int = 0,
        pauses: List<PauseRecord> = emptyList(),
        restless: Duration = Duration.ZERO,
        duration: Duration = 40.minutes,
    ) = Segment(
        id = SegmentId("s"),
        activity = Activity(ActivityId("a"), ActivityCategory.STUDY, name = null),
        start = start,
        end = start.plusSeconds(duration.inWholeSeconds),
        planned = 40.minutes,
        levelTime = levels,
        restlessTime = restless,
        pauseSuggestions = suggestions,
        pauses = pauses,
    )
}
