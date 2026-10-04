package com.stateai.domain.score

import com.stateai.domain.segment.Segment
import kotlin.time.Duration

/**
 * Scores one segment (SPEC 6.6). Each component goes from 0 to 100:
 * - focus: share of the estimated time spent at `LOW` activation;
 * - recovery: share of the needed pauses that were taken and improved the state (100 when none was needed);
 *   pauses without estimates on both sides cannot be judged and are left out;
 * - sustainable load: 100 minus the share of time at `HIGH` or restless;
 * - consistency: how much of the planned block was completed.
 */
class SegmentScorer {
    fun score(segment: Segment): ScoreBreakdown = ScoreBreakdown(
        focus = shareOf(segment.levelTime.low, segment.levelTime.known),
        recovery = recovery(segment),
        sustainableLoad = MAX - shareOf(loadTime(segment), segment.levelTime.known),
        consistency = shareOf(segment.duration, segment.planned).coerceAtMost(MAX),
    )

    private fun recovery(segment: Segment): Double {
        val evaluable = segment.pauses.filter { it.isEvaluable }
        val needed = maxOf(segment.pauseSuggestions, evaluable.size)
        if (needed == 0) return MAX
        return MAX * evaluable.count { it.improved } / needed
    }

    /** High activation and restlessness can overlap; their sum is capped at the known time. */
    private fun loadTime(segment: Segment): Duration =
        minOf(segment.levelTime.high + segment.restlessTime, segment.levelTime.known)

    private fun shareOf(part: Duration, whole: Duration): Double =
        if (whole <= Duration.ZERO) 0.0 else (MAX * (part / whole)).coerceIn(0.0, Double.MAX_VALUE)

    private companion object {
        const val MAX = 100.0
    }
}
