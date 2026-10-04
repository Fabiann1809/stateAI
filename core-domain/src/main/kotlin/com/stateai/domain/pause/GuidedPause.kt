package com.stateai.domain.pause

import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.state.StateEstimate
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Breathing rhythm of the guided pause (SPEC 6.2): inhale 4 s, exhale 6 s. */
data class BreathingRhythm(val inhale: Duration = 4.seconds, val exhale: Duration = 6.seconds) {
    val cycle: Duration get() = inhale + exhale
}

/**
 * Tracks a guided pause within the running session and records it with the estimates right before
 * and right after, so the score can tell whether the pause helped.
 */
class GuidedPause(private val recorder: SegmentRecorder, private val clock: Clock) {
    private var startedAt: Instant? = null
    private var before: StateEstimate? = null

    fun begin() {
        startedAt = clock.instant()
        before = recorder.latestEstimate
    }

    fun end(): PauseRecord? {
        val start = startedAt ?: return null
        startedAt = null
        val pause = PauseRecord(start, clock.instant(), before, recorder.latestEstimate)
        recorder.onPause(pause)
        return pause
    }
}
