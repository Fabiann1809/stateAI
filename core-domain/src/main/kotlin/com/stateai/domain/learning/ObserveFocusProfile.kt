package com.stateai.domain.learning

import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The focus profile of the recent past (by default the last four weeks), updated with each segment. */
class ObserveFocusProfile(
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val history: Duration = Duration.ofDays(DEFAULT_HISTORY_DAYS),
) {
    private val builder = FocusProfileBuilder(clock.zone)

    operator fun invoke(): Flow<FocusProfile> {
        val now = clock.instant()
        return segments.observeBetween(now.minus(history), now).map(builder::build)
    }

    private companion object {
        const val DEFAULT_HISTORY_DAYS = 28L
    }
}
