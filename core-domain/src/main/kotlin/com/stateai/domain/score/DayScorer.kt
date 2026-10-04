package com.stateai.domain.score

import com.stateai.domain.segment.DayRecord

/**
 * Daily score: every component is the duration-weighted average of the day's segments (SPEC 6.6).
 * Segments without any estimated time (e.g. ended during calibration) carry no evidence and are skipped.
 */
class DayScorer(private val segmentScorer: SegmentScorer = SegmentScorer()) {
    /** Returns null for a day without estimated time. */
    fun score(day: DayRecord): ScoreBreakdown? {
        val weighted = day.segments.filter { it.levelTime.known.isPositive() }.map {
            it.duration.inWholeSeconds.toDouble() to
                segmentScorer.score(it)
        }
        val totalSeconds = weighted.sumOf { it.first }
        if (totalSeconds <= 0.0) return null
        fun average(component: (ScoreBreakdown) -> Double) =
            weighted.sumOf { (seconds, breakdown) -> seconds * component(breakdown) } / totalSeconds
        return ScoreBreakdown(
            focus = average { it.focus },
            recovery = average { it.recovery },
            sustainableLoad = average { it.sustainableLoad },
            consistency = average { it.consistency },
        )
    }
}
