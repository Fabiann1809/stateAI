package com.stateai.domain.summary

import com.stateai.domain.score.DayScorer
import com.stateai.domain.score.ScoreBreakdown
import com.stateai.domain.segment.DayRecord
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Score of one day; null when the day has no estimated time. */
data class DayScore(val date: LocalDate, val score: ScoreBreakdown?)

/** Scores of the last [days] days, oldest first and ending today, updated as segments are saved. */
class ObserveRecentDays(
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val days: Int = DEFAULT_DAYS,
    private val scorer: DayScorer = DayScorer(),
) {
    operator fun invoke(today: LocalDate = LocalDate.now(clock)): Flow<List<DayScore>> {
        val first = today.minusDays(days - 1L)
        val from = first.atStartOfDay(clock.zone).toInstant()
        val to = today.plusDays(1).atStartOfDay(clock.zone).toInstant()
        return segments.observeBetween(from, to).map { recent ->
            val byDate = recent.groupBy { it.start.atZone(clock.zone).toLocalDate() }
            (0 until days).map { offset ->
                val date = first.plusDays(offset.toLong())
                DayScore(date, scorer.score(DayRecord(date, byDate[date].orEmpty())))
            }
        }
    }

    private companion object {
        const val DEFAULT_DAYS = 7
    }
}

/** Today's components minus yesterday's, or null when either day has no score. */
fun List<DayScore>.changeSinceYesterday(): ScoreBreakdown? {
    val today = lastOrNull()?.score
    val yesterday = getOrNull(size - 2)?.score
    if (today == null || yesterday == null) return null
    return ScoreBreakdown(
        focus = today.focus - yesterday.focus,
        recovery = today.recovery - yesterday.recovery,
        sustainableLoad = today.sustainableLoad - yesterday.sustainableLoad,
        consistency = today.consistency - yesterday.consistency,
    )
}
