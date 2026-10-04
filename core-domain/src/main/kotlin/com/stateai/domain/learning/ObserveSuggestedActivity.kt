package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The activity suggested for right now, from the last weeks of segments, or null. */
class ObserveSuggestedActivity(
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val history: Duration = Duration.ofDays(DEFAULT_HISTORY_DAYS),
) {
    private val predictor = ActivityPredictor(clock.zone)

    operator fun invoke(): Flow<ActivityId?> {
        val now = clock.instant()
        return segments.observeBetween(now.minus(history), now).map { recent ->
            predictor.predict(recent, ZonedDateTime.now(clock))
        }
    }

    private companion object {
        const val DEFAULT_HISTORY_DAYS = 28L
    }
}
