package com.stateai.domain.learning

/** Confidence and margin required before calling an hour a focus window. */
data class FocusWindowConfig(val minDays: Int = 5, val minWindows: Int = 30, val marginOverAverage: Double = 0.1)

/**
 * Says whether an hour of the day is a good focus window: it must have data from at least
 * [FocusWindowConfig.minDays] different days and a focus share clearly above the person's average.
 * Uses the hour across all weekdays, which needs far less data than hour-and-weekday slots.
 */
class FocusWindowAdvisor(private val config: FocusWindowConfig = FocusWindowConfig()) {
    fun isFocusWindow(profile: FocusProfile, hour: Int): Boolean {
        val stats = profile.byHour[hour] ?: return false
        val confident = stats.days >= config.minDays && stats.windows >= config.minWindows
        return confident && stats.focusShare >= profile.overallFocusShare + config.marginOverAverage
    }
}
