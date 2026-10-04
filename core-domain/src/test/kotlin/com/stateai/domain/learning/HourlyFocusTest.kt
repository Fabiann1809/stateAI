package com.stateai.domain.learning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class HourlyFocusTest {
    @Test
    fun `fills the hours between the first and last observed and finds the best`() {
        val profile = FocusProfile(
            bySlot = emptyMap(),
            byHour = mapOf(8 to SlotStats(0.4, 30, 3), 10 to SlotStats(0.9, 40, 6), 11 to SlotStats(0.6, 20, 2)),
            overallFocusShare = 0.6,
        )

        val hourly = HourlyFocus.of(profile)

        assertEquals(listOf(8, 9, 10, 11), hourly.hours.map { it.hour })
        assertEquals(0.0, hourly.hours[1].share)
        assertEquals(10, hourly.bestHour)
        assertEquals(6, hourly.bestHourDays)
    }

    @Test
    fun `no data gives no hours and no best hour`() {
        val hourly = HourlyFocus.of(FocusProfile.EMPTY)

        assertEquals(emptyList<HourFocus>(), hourly.hours)
        assertNull(hourly.bestHour)
    }
}
