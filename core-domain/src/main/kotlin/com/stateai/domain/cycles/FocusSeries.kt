package com.stateai.domain.cycles

import com.stateai.domain.segment.Segment
import com.stateai.domain.state.ActivationLevel
import java.time.Instant
import kotlin.math.abs

/** Focus observations of one session: minutes from a common origin and the focus residual. */
class SessionSeries(val minutes: DoubleArray, val values: DoubleArray) {
    val size: Int get() = minutes.size
}

/**
 * Builds the focus series used for cycle detection from segment traces.
 *
 * - Window `i` of a segment is placed `i + 1` minutes after its start; windows without an estimate
 *   and windows within [cueExclusionMinutes] of a cue the app played are skipped.
 * - Focus is 1 for `LOW` and 0 otherwise. The mean focus at each minute-since-session-start, across
 *   all sessions, is subtracted. That removes every effect locked to the session itself (warm-up,
 *   the end-of-block cue), which would otherwise look like a rhythm when sessions start at similar
 *   times every day. A rhythm of the person, whose phase does not follow session starts, remains.
 */
class FocusSeriesBuilder(private val cueExclusionMinutes: Int = DEFAULT_CUE_EXCLUSION_MINUTES) {
    private class Point(val absoluteMinute: Double, val sessionMinute: Int, val focus: Double)

    fun build(segments: List<Segment>): List<SessionSeries> {
        val origin = segments.minOfOrNull { it.start } ?: return emptyList()
        val sessions = segments.map { pointsOf(it, origin) }.filter { it.isNotEmpty() }
        val sessionProfile = sessions.flatten().groupBy { it.sessionMinute }
            .mapValues { (_, points) -> points.map { it.focus }.average() }
        return sessions.map { points ->
            SessionSeries(
                minutes = points.map { it.absoluteMinute }.toDoubleArray(),
                values = points.map { it.focus - sessionProfile.getValue(it.sessionMinute) }.toDoubleArray(),
            )
        }
    }

    private fun pointsOf(segment: Segment, origin: Instant): List<Point> {
        val offset = java.time.Duration.between(origin, segment.start).seconds / SECONDS_PER_MINUTE
        return segment.trace.levels().mapIndexedNotNull { index, level ->
            val minute = index + 1
            if (level == null || isNearCue(minute, segment.cueMinutes)) return@mapIndexedNotNull null
            Point(offset + minute, minute, if (level == ActivationLevel.LOW) FOCUSED else NOT_FOCUSED)
        }
    }

    private fun isNearCue(minute: Int, cues: List<Int>) = cues.any { abs(it - minute) <= cueExclusionMinutes }

    private companion object {
        const val DEFAULT_CUE_EXCLUSION_MINUTES = 2
        const val SECONDS_PER_MINUTE = 60.0
        const val FOCUSED = 1.0
        const val NOT_FOCUSED = 0.0
    }
}
