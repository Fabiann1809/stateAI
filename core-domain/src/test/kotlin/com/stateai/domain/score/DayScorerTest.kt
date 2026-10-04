package com.stateai.domain.score

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.DayRecord
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DayScorerTest {
    private val scorer = DayScorer()
    private val day = LocalDate.of(2026, 10, 4)
    private val start = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `day score is weighted by segment duration`() {
        // Focus components: 100 over 10 min, 50 over 20 min, 0 over 30 min.
        val segments = listOf(
            segment(minutes = 10, low = 10),
            segment(minutes = 20, low = 10),
            segment(minutes = 30, low = 0),
        )

        val breakdown = scorer.score(DayRecord(day, segments))!!

        assertEquals((10 * 100.0 + 20 * 50.0) / 60, breakdown.focus, 1e-9)
    }

    @Test
    fun `day with equal segments averages them`() {
        val segments = listOf(segment(minutes = 30, low = 30), segment(minutes = 30, low = 0))

        assertEquals(50.0, scorer.score(DayRecord(day, segments))!!.focus, 1e-9)
    }

    @Test
    fun `segments without estimated time are skipped`() {
        val calibrationOnly = segment(minutes = 30, low = 0).let {
            it.copy(id = SegmentId("calibration"), levelTime = LevelDurations(unknown = 30.minutes))
        }
        val segments = listOf(segment(minutes = 30, low = 30), calibrationOnly)

        assertEquals(100.0, scorer.score(DayRecord(day, segments))!!.focus, 1e-9)
    }

    @Test
    fun `empty day has no score`() {
        assertNull(scorer.score(DayRecord(day, emptyList())))
    }

    private fun segment(minutes: Int, low: Int): Segment {
        val duration = minutes.minutes
        return Segment(
            id = SegmentId("$minutes-$low"),
            activity = Activity(ActivityId("a"), ActivityCategory.STUDY, name = null),
            start = start,
            end = start.plusSeconds(duration.inWholeSeconds),
            planned = duration,
            levelTime = LevelDurations(low = low.minutes, medium = duration - low.minutes),
            restlessTime = Duration.ZERO,
            pauseSuggestions = 0,
            pauses = emptyList(),
        )
    }
}
