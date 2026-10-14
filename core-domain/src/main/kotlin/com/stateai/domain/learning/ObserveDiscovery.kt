package com.stateai.domain.learning

import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The discovery from the ratings of the recent past (by default the last four weeks). */
class ObserveDiscovery(
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val history: Duration = Duration.ofDays(DEFAULT_HISTORY_DAYS),
) {
    private val finder = DiscoveryFinder(clock.zone)

    operator fun invoke(): Flow<Discovery> {
        val now = clock.instant()
        return segments.observeBetween(now.minus(history), now).map(finder::find)
    }

    private companion object {
        const val DEFAULT_HISTORY_DAYS = 28L
    }
}
