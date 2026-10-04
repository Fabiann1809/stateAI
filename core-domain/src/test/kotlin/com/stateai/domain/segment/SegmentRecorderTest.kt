package com.stateai.domain.segment

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.session.ActiveSession
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SegmentRecorderTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")
    private val study = Activity(ActivityId("study"), ActivityCategory.STUDY, name = null)
    private val session = ActiveSession(study, DefaultCategoryProfiles.STUDY, startedAt = start)
    private val recorder = SegmentRecorder { SegmentId("segment") }

    @Test
    fun `attributes each stretch to the estimate in force`() {
        recorder.start(session)
        recorder.onEstimate(StateEstimate(ActivationLevel.LOW, false), at(3))
        recorder.onEstimate(StateEstimate(ActivationLevel.HIGH, true), at(13))

        val segment = recorder.finish(at(20))!!

        assertEquals(LevelDurations(low = 10.minutes, high = 7.minutes, unknown = 3.minutes), segment.levelTime)
        assertEquals(7.minutes, segment.restlessTime)
        assertEquals(20.minutes, segment.duration)
        assertEquals(40.minutes, segment.planned)
    }

    @Test
    fun `counts pause suggestions and keeps pauses`() {
        recorder.start(session)
        recorder.onPauseSuggested()
        val pause = PauseRecord(at(5), at(7), StateEstimate(ActivationLevel.HIGH, false), null)
        recorder.onPause(pause)

        val segment = recorder.finish(at(10))!!

        assertEquals(1, segment.pauseSuggestions)
        assertEquals(listOf(pause), segment.pauses)
    }

    @Test
    fun `starting again resets the summary`() {
        recorder.start(session)
        recorder.onEstimate(StateEstimate(ActivationLevel.HIGH, false), at(1))
        recorder.start(session.copy(startedAt = at(10)))

        assertEquals(Duration.ZERO, recorder.finish(at(12))!!.levelTime.high)
    }

    @Test
    fun `finishing without a session gives nothing`() {
        assertNull(recorder.finish(at(1)))
    }

    @Test
    fun `a pause improves when activation drops or restlessness goes away`() {
        val high = StateEstimate(ActivationLevel.HIGH, false)
        val low = StateEstimate(ActivationLevel.LOW, false)
        val restless = StateEstimate(ActivationLevel.LOW, true)

        assertTrue(PauseRecord(at(0), at(2), high, low).improved)
        assertTrue(PauseRecord(at(0), at(2), restless, low).improved)
        assertFalse(PauseRecord(at(0), at(2), low, high).improved)
        assertFalse(PauseRecord(at(0), at(2), null, low).improved)
    }

    private fun at(minute: Long): Instant = start.plusSeconds(minute * 60)
}
