package com.stateai.domain.segment

import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.session.ActiveSession
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Accumulates the summary of the running session: time per level (each stretch is attributed to the
 * estimate in force), restless time, pauses, the per-window trace and the calm and movement means.
 */
class SegmentRecorder(private val newId: () -> SegmentId) {
    private var session: ActiveSession? = null
    private var lastMark: Instant = Instant.EPOCH
    private var currentEstimate: StateEstimate? = null
    private var levelTime = LevelDurations()
    private var restlessTime = Duration.ZERO
    private var pauseSuggestions = 0
    private val pauses = mutableListOf<PauseRecord>()
    private var trace = LevelTrace()
    private val cueMinutes = mutableListOf<Int>()
    private val calmHeartRates = mutableListOf<Double>()
    private val cleanMovements = mutableListOf<Double>()

    val latestEstimate: StateEstimate? get() = currentEstimate

    fun start(session: ActiveSession) {
        this.session = session
        lastMark = session.startedAt
        currentEstimate = null
        levelTime = LevelDurations()
        restlessTime = Duration.ZERO
        pauseSuggestions = 0
        pauses.clear()
        trace = LevelTrace()
        cueMinutes.clear()
        calmHeartRates.clear()
        cleanMovements.clear()
    }

    /** Records one feature window; [estimate] is null when the window gave no usable estimate. */
    fun onWindow(window: FeatureWindow, estimate: StateEstimate?) {
        if (session == null) return
        trace = trace.plus(estimate?.level)
        if (estimate == null) return
        cleanMovements += window.meanMovement
        val calm = estimate.level == ActivationLevel.LOW && !estimate.restless
        if (calm) window.meanHeartRate?.let { calmHeartRates += it }
        onEstimate(estimate, window.end)
    }

    fun onEstimate(estimate: StateEstimate, at: Instant) {
        advanceTo(at)
        currentEstimate = estimate
    }

    /** Marks a time cue played by the app, so cycle detection can discount it. */
    fun onCue(at: Instant) {
        val running = session ?: return
        cueMinutes += java.time.Duration.between(running.startedAt, at).toMinutes().toInt()
    }

    fun onPauseSuggested() {
        pauseSuggestions++
    }

    fun onPause(pause: PauseRecord) {
        pauses += pause
    }

    /** Closes the running segment, or returns null when no session is being recorded. */
    fun finish(at: Instant): Segment? {
        val running = session ?: return null
        advanceTo(at)
        session = null
        return Segment(
            id = newId(),
            activity = running.activity,
            start = running.startedAt,
            end = at,
            planned = running.targetBlock,
            levelTime = levelTime,
            restlessTime = restlessTime,
            pauseSuggestions = pauseSuggestions,
            pauses = pauses.toList(),
            trace = trace,
            cueMinutes = cueMinutes.toList(),
            calmHeartRate = calmHeartRates.takeIf { it.isNotEmpty() }?.average(),
            cleanMovement = cleanMovements.takeIf { it.isNotEmpty() }?.average(),
        )
    }

    private fun advanceTo(at: Instant) {
        if (session == null || !at.isAfter(lastMark)) return
        val stretch = java.time.Duration.between(lastMark, at).toKotlinDuration()
        levelTime = levelTime.plus(currentEstimate?.level, stretch)
        if (currentEstimate?.restless == true) restlessTime += stretch
        lastMark = at
    }
}
