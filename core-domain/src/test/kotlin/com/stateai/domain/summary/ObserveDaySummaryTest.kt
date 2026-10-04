package com.stateai.domain.summary

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.learning.ActivityLearning
import com.stateai.domain.learning.LearningProgress
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.testing.FakeLearningRepository
import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.MutableClock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ObserveDaySummaryTest {
    private val clock = MutableClock(Instant.parse("2026-10-04T18:00:00Z"))
    private val segments = FakeSegmentRepository()
    private val observe = ObserveDaySummary(segments, clock)

    @Test
    fun `summarizes only today's segments`() = runTest {
        segments.save(segment("2026-10-03T09:00:00Z", low = 30))
        segments.save(segment("2026-10-04T09:00:00Z", low = 30))
        segments.save(segment("2026-10-04T15:00:00Z", low = 10))

        val summary = observe().first()

        assertEquals(2, summary.segments.size)
        assertEquals(60.minutes, summary.totalTime)
    }

    @Test
    fun `best hour is the one with the highest focus share`() = runTest {
        segments.save(segment("2026-10-04T09:00:00Z", low = 10))
        segments.save(segment("2026-10-04T15:00:00Z", low = 25))

        assertEquals(15, observe().first().bestHour)
    }

    @Test
    fun `shows the learning hint while today's activities have few sessions`() = runTest {
        val learning = FakeLearningRepository()
        val withProgress = ObserveDaySummary(segments, clock, LearningProgress(learning))
        segments.save(segment("2026-10-04T09:00:00Z", low = 30))

        assertTrue(withProgress().first().isLearning)

        learning.save(ActivityLearning(ActivityId("a"), validSessions = 5))
        assertFalse(withProgress().first().isLearning)
    }

    @Test
    fun `empty day has no score and no best hour`() = runTest {
        val summary = observe().first()

        assertNull(summary.score)
        assertNull(summary.bestHour)
    }

    private fun segment(start: String, low: Int): Segment {
        val begin = Instant.parse(start)
        return Segment(
            id = SegmentId(start),
            activity = Activity(ActivityId("a"), ActivityCategory.STUDY, name = null),
            start = begin,
            end = begin.plusSeconds(30 * 60),
            planned = 30.minutes,
            levelTime = LevelDurations(low = low.minutes, medium = (30 - low).minutes),
            restlessTime = Duration.ZERO,
            pauseSuggestions = 0,
            pauses = emptyList(),
        )
    }
}
