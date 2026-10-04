package com.stateai.domain.cycles

import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/** Cycle detection result and on how many days of history it is based. */
data class CycleInsight(val result: CycleResult, val days: Int)

/**
 * Runs cycle detection on the recent history (four weeks by default) whenever segments change. The
 * permutation test is CPU heavy, so it runs on [dispatcher], never on the main thread.
 */
class ObserveCycle(
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val history: Duration = Duration.ofDays(DEFAULT_HISTORY_DAYS),
    private val detector: CycleDetector = CycleDetector(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    operator fun invoke(): Flow<CycleInsight> {
        val now = clock.instant()
        return segments.observeBetween(now.minus(history), now.plus(Duration.ofDays(1)))
            .map { recent ->
                val days = recent.map { it.start.atZone(clock.zone).toLocalDate() }.distinct().size
                CycleInsight(detector.detect(recent), days)
            }
            .flowOn(dispatcher)
    }

    private companion object {
        const val DEFAULT_HISTORY_DAYS = 28L
    }
}
