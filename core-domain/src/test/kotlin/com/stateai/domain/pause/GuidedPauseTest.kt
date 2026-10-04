package com.stateai.domain.pause

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.session.ActiveSession
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import com.stateai.domain.testing.MutableClock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GuidedPauseTest {
    private val clock = MutableClock()
    private val recorder = SegmentRecorder { SegmentId("s") }.apply {
        start(
            ActiveSession(
                Activity(ActivityId("a"), ActivityCategory.STUDY, null),
                DefaultCategoryProfiles.STUDY,
                clock.instant(),
            ),
        )
    }
    private val pause = GuidedPause(recorder, clock)

    @Test
    fun `records the pause with the estimates before and after`() {
        recorder.onEstimate(StateEstimate(ActivationLevel.HIGH, false), clock.instant())
        pause.begin()
        clock.advanceBy(2.minutes)
        recorder.onEstimate(StateEstimate(ActivationLevel.LOW, false), clock.instant())

        val record = pause.end()!!

        assertTrue(record.improved)
        assertEquals(listOf(record), recorder.finish(clock.instant())!!.pauses)
    }

    @Test
    fun `ending without beginning records nothing`() {
        assertNull(pause.end())
    }

    @Test
    fun `one breath lasts ten seconds`() {
        assertEquals(10.seconds, BreathingRhythm().cycle)
    }
}
