package com.stateai.domain.learning

import java.time.DayOfWeek

/** A one-hour slot of the week. */
data class TimeSlot(val dayOfWeek: DayOfWeek, val hour: Int) {
    init {
        require(hour in 0..LAST_HOUR) { "Hour must be between 0 and 23" }
    }

    private companion object {
        const val LAST_HOUR = 23
    }
}

/** Focus statistics of a slot: share of usable windows at `LOW`, and how much data supports it. */
data class SlotStats(val focusShare: Double, val windows: Int, val days: Int)

/** Learned focus by time of week (SPEC 7.3). Built from segment traces, never from raw signal. */
data class FocusProfile(
    val bySlot: Map<TimeSlot, SlotStats>,
    val byHour: Map<Int, SlotStats>,
    val overallFocusShare: Double,
) {
    companion object {
        val EMPTY = FocusProfile(emptyMap(), emptyMap(), overallFocusShare = 0.0)
    }
}
