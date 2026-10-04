package com.stateai.domain.session

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.profile.CategoryDefaultsProfileProvider
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.testing.FakeActivityRepository
import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.MutableClock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class StartSessionTest {
    private val study = Activity(ActivityId("study"), ActivityCategory.STUDY, name = null)
    private val reading = Activity(ActivityId("reading"), ActivityCategory.READING, name = null)
    private val repository = FakeActivityRepository(listOf(study, reading))
    private val segments = FakeSegmentRepository()
    private val tracker = SessionTracker()
    private val clock = MutableClock()
    private var nextSegment = 0
    private val recorder = SegmentRecorder { SegmentId("${nextSegment++}") }
    private val endSession = EndSession(tracker, recorder, segments, clock)
    private val startSession =
        StartSession(repository, tracker, CategoryDefaultsProfileProvider(), recorder, endSession, clock)

    @Test
    fun `starts a session with the category target block`() = runTest {
        val session = startSession(study.id)

        assertEquals(40.minutes, session?.targetBlock)
        assertSame(session, tracker.activeSession.value)
    }

    @Test
    fun `marks the activity as used`() = runTest {
        startSession(study.id)

        assertEquals(clock.instant(), repository.lastUsed[study.id])
    }

    @Test
    fun `keeps the running session for the same activity`() = runTest {
        val first = startSession(study.id)
        clock.advanceBy(5.minutes)

        assertSame(first, startSession(study.id))
        assertEquals(0, segments.all().size)
    }

    @Test
    fun `switching activity closes the running segment`() = runTest {
        startSession(study.id)
        clock.advanceBy(12.minutes)

        assertEquals(reading, startSession(reading.id)?.activity)
        val closed = segments.all().single()
        assertEquals(study, closed.activity)
        assertEquals(12.minutes, closed.duration)
    }

    @Test
    fun `ending the session stores its segment and clears the tracker`() = runTest {
        startSession(study.id)
        clock.advanceBy(30.minutes)

        endSession()

        assertEquals(30.minutes, segments.all().single().duration)
        assertNull(tracker.activeSession.value)
    }

    @Test
    fun `returns null for an unknown activity`() = runTest {
        assertNull(startSession(ActivityId("missing")))
    }

    @Test
    fun `elapsed time follows the clock`() = runTest {
        val session = startSession(study.id)!!
        clock.advanceBy(12.minutes)

        assertEquals(12.minutes, session.progressAt(clock.instant()).elapsed)
    }
}
