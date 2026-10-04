package com.stateai.domain.learning

import com.stateai.domain.segment.Segment
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * A session counts towards an activity's learning (`n` in SPEC 6.7) only if it lasted at least
 * [minDuration] and at least [minCleanShare] of its windows were clean. Cut or noisy sessions do not count.
 */
data class ValidSessionRule(val minDuration: Duration = 10.minutes, val minCleanShare: Double = 0.6) {
    fun isValid(segment: Segment): Boolean {
        val windows = segment.trace.windowCount
        if (segment.duration < minDuration || windows == 0) return false
        return segment.trace.cleanWindowCount.toDouble() / windows >= minCleanShare
    }
}
