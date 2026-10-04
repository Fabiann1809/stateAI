package com.stateai.ui.common

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DurationFormatTest {
    @Test
    fun `formats minutes and seconds below one hour`() {
        assertEquals("7:05", (7.minutes + 5.seconds).toClockText())
    }

    @Test
    fun `includes hours from one hour on`() {
        assertEquals("1:02:03", (1.hours + 2.minutes + 3.seconds).toClockText())
    }

    @Test
    fun `zero is shown as zero minutes`() {
        assertEquals("0:00", 0.seconds.toClockText())
    }

    @Test
    fun `hours and minutes text drops empty parts`() {
        assertEquals("40 min", 40.minutes.toHoursMinutesText())
        assertEquals("2 h", 2.hours.toHoursMinutesText())
        assertEquals("1 h 40 min", (1.hours + 40.minutes).toHoursMinutesText())
    }
}
