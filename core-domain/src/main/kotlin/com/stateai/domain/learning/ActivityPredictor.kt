package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.Segment
import java.time.ZoneId
import java.time.ZonedDateTime

/** How routine predictions weigh past segments and how much support they need. */
data class PredictionConfig(val sameSlotWeight: Int = 2, val sameHourWeight: Int = 1, val minScore: Int = 4)

/**
 * Predicts the activity the person usually starts at this time (SPEC 7.5): segments that started in
 * the same weekday and hour count double, segments in the same hour on other days count once.
 * Returns null when no activity has enough support.
 */
class ActivityPredictor(private val zone: ZoneId, private val config: PredictionConfig = PredictionConfig()) {
    fun predict(segments: List<Segment>, now: ZonedDateTime): ActivityId? {
        val scores = segments.groupBy { it.activity.id }.mapValues { (_, activitySegments) ->
            activitySegments.sumOf { weightOf(it, now) }
        }
        return scores.filterValues { it >= config.minScore }.maxByOrNull { it.value }?.key
    }

    private fun weightOf(segment: Segment, now: ZonedDateTime): Int {
        val start = segment.start.atZone(zone)
        return when {
            start.hour != now.hour -> 0
            start.dayOfWeek == now.dayOfWeek -> config.sameSlotWeight
            else -> config.sameHourWeight
        }
    }
}
