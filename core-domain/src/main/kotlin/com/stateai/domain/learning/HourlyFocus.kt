package com.stateai.domain.learning

/** Share of focus in one hour of the day. */
data class HourFocus(val hour: Int, val share: Double)

/**
 * Focus by hour of the day for the chart: every hour between the first and the last hour with data
 * (empty hours count as 0), the best hour, and on how many days that hour was observed. Only hours
 * seen on at least [MIN_DAYS] days can be the best one, so a single good session does not win.
 */
data class HourlyFocus(val hours: List<HourFocus>, val bestHour: Int?, val bestHourDays: Int) {
    companion object {
        const val MIN_DAYS = 3
        val EMPTY = HourlyFocus(emptyList(), bestHour = null, bestHourDays = 0)

        fun of(profile: FocusProfile): HourlyFocus {
            val byHour = profile.byHour
            if (byHour.isEmpty()) return EMPTY
            val best = byHour.filterValues {
                it.focusShare > 0.0 && it.days >= MIN_DAYS
            }.maxByOrNull { it.value.focusShare }
            return HourlyFocus(
                hours = (byHour.keys.min()..byHour.keys.max()).map { HourFocus(it, byHour[it]?.focusShare ?: 0.0) },
                bestHour = best?.key,
                bestHourDays = best?.value?.days ?: 0,
            )
        }
    }
}
