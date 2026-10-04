package com.stateai.domain.summary

import com.stateai.domain.learning.LearningProgress
import com.stateai.domain.score.DayScorer
import com.stateai.domain.segment.DayRecord
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Summary of today (in the clock's time zone), updated as segments are saved. */
class ObserveDaySummary(
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val learningProgress: LearningProgress? = null,
    private val scorer: DayScorer = DayScorer(),
) {
    operator fun invoke(date: LocalDate = LocalDate.now(clock)): Flow<DaySummary> {
        val from = date.atStartOfDay(clock.zone).toInstant()
        val to = date.plusDays(1).atStartOfDay(clock.zone).toInstant()
        return segments.observeBetween(from, to).map { daySegments ->
            DaySummary(
                date = date,
                segments = daySegments,
                score = scorer.score(DayRecord(date, daySegments)),
                bestHour = bestHour(daySegments),
                isLearning = learningProgress?.isLearning(daySegments.map { it.activity.id }.distinct()) ?: false,
            )
        }
    }

    /** Groups segments by start hour and picks the hour with the highest share of focus time. */
    private fun bestHour(daySegments: List<Segment>): Int? = daySegments
        .groupBy { it.start.atZone(clock.zone).hour }
        .mapValues { (_, hourSegments) ->
            val known = hourSegments.sumOf { it.levelTime.known.inWholeSeconds }
            val low = hourSegments.sumOf { it.levelTime.low.inWholeSeconds }
            if (known == 0L) 0.0 else low.toDouble() / known
        }
        .filterValues { it > 0.0 }
        .maxByOrNull { it.value }
        ?.key
}
