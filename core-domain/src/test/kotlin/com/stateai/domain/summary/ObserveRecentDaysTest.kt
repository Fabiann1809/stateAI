package com.stateai.domain.summary

import com.stateai.domain.score.ScoreBreakdown
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.testSegment
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ObserveRecentDaysTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-07T18:00:00Z"), ZoneOffset.UTC)
    private val repository = FakeSegmentRepository()

    @Test
    fun `gives seven days ending today, with empty days unscored`() = runTest {
        repository.save(testSegment(id = "today", start = Instant.parse("2026-10-07T09:00:00Z")))
        repository.save(testSegment(id = "monday", start = Instant.parse("2026-10-05T09:00:00Z")))
        repository.save(testSegment(id = "too old", start = Instant.parse("2026-09-30T09:00:00Z")))

        val days = ObserveRecentDays(repository, clock)().first()

        assertEquals(LocalDate.parse("2026-10-01"), days.first().date)
        assertEquals(LocalDate.parse("2026-10-07"), days.last().date)
        assertEquals(7, days.size)
        assertNotNull(days.last().score)
        assertNotNull(days[4].score)
        assertNull(days.first().score)
    }

    @Test
    fun `change since yesterday subtracts each component`() = runTest {
        val yesterday = Instant.parse("2026-10-06T09:00:00Z")
        repository.save(testSegment(id = "yesterday", start = yesterday, levelTime = half()))
        repository.save(testSegment(id = "today", start = Instant.parse("2026-10-07T09:00:00Z")))

        val change = ObserveRecentDays(repository, clock)().first().changeSinceYesterday()

        assertEquals(50.0, change!!.focus, 1e-9)
    }

    @Test
    fun `no change without a score yesterday`() {
        val days = listOf(
            DayScore(LocalDate.parse("2026-10-06"), null),
            DayScore(LocalDate.parse("2026-10-07"), ScoreBreakdown(70.0, 60.0, 80.0, 90.0)),
        )

        assertNull(days.changeSinceYesterday())
    }

    private fun half() = LevelDurations(low = 20.minutes, medium = 20.minutes)
}
