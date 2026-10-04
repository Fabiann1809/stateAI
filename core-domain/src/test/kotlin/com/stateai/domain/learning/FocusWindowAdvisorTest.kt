package com.stateai.domain.learning

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FocusWindowAdvisorTest {
    private val advisor = FocusWindowAdvisor()

    @Test
    fun `a strong hour backed by five days is a focus window`() {
        assertTrue(advisor.isFocusWindow(profile(focus = 0.9, days = 5), hour = 9))
    }

    @Test
    fun `fewer than five days is not enough confidence`() {
        assertFalse(advisor.isFocusWindow(profile(focus = 0.9, days = 4), hour = 9))
    }

    @Test
    fun `an hour close to the average is not special`() {
        assertFalse(advisor.isFocusWindow(profile(focus = 0.65, days = 10), hour = 9))
    }

    @Test
    fun `an hour without data is not a focus window`() {
        assertFalse(advisor.isFocusWindow(profile(focus = 0.9, days = 10), hour = 14))
    }

    private fun profile(focus: Double, days: Int) = FocusProfile(
        bySlot = emptyMap(),
        byHour = mapOf(9 to SlotStats(focusShare = focus, windows = days * 30, days = days)),
        overallFocusShare = 0.6,
    )
}
