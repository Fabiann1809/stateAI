package com.stateai.domain.summary

import com.stateai.domain.score.ScoreBreakdown
import com.stateai.domain.segment.Segment
import java.time.LocalDate
import kotlin.time.Duration

/** What the daily summary screen shows. */
data class DaySummary(
    val date: LocalDate,
    val segments: List<Segment>,
    val score: ScoreBreakdown?,
    /** Hour of the day (0-23) whose segments had the highest focus share, if any. */
    val bestHour: Int?,
) {
    val totalTime: Duration get() = segments.fold(Duration.ZERO) { total, segment -> total + segment.duration }
}
