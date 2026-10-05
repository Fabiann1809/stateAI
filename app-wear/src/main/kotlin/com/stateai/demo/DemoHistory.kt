package com.stateai.demo

import com.stateai.domain.activity.Activity
import com.stateai.domain.segment.Segment
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

/**
 * Synthetic history for the demo: the [days] days before today plus today's sessions that have
 * already ended by [segments]' `now`, as segments the watch would have stored. Each day has its own
 * seed, so a day is the same whenever it is generated. It is simulated data and is labelled as such
 * wherever it is shown.
 */
class DemoHistory(
    private val zone: ZoneId,
    private val days: Int = DEFAULT_DAYS,
    private val seed: Int = DEFAULT_SEED,
) {
    fun segments(now: Instant, activities: Map<DemoActivity, Activity>): List<Segment> {
        val today = now.atZone(zone).toLocalDate()
        return (days downTo 0).flatMap { back ->
            val date = today.minusDays(back.toLong())
            DemoDay(zone, date, Random(seed * SEED_SPREAD + date.toEpochDay())).segments(activities)
        }.filter { !it.end.isAfter(now) }
    }

    private companion object {
        const val DEFAULT_DAYS = 14
        const val DEFAULT_SEED = 14
        const val SEED_SPREAD = 100_000L
    }
}
