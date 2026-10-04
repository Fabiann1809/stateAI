package com.stateai.domain.haptics

import com.stateai.domain.testing.MutableClock
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HapticRateLimiterTest {
    private val clock = MutableClock()
    private val limiter = HapticRateLimiter(clock)

    @Test
    fun `allows the first vibration`() {
        assertTrue(limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3))
    }

    @Test
    fun `blocks a second vibration within five minutes`() {
        limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3)
        clock.advanceBy(4.minutes)

        assertFalse(limiter.tryAcquire(HapticEvent.OVERLOAD_ALERT, maxPerHour = 3))
    }

    @Test
    fun `allows a vibration once five minutes have passed`() {
        limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3)
        clock.advanceBy(5.minutes)

        assertTrue(limiter.tryAcquire(HapticEvent.OVERLOAD_ALERT, maxPerHour = 3))
    }

    @Test
    fun `enforces the hourly cap of the profile`() {
        repeat(2) {
            limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 2)
            clock.advanceBy(10.minutes)
        }

        assertFalse(limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 2))
    }

    @Test
    fun `hourly cap frees up as old vibrations leave the window`() {
        repeat(2) {
            limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 2)
            clock.advanceBy(10.minutes)
        }
        clock.advanceBy(41.minutes)

        assertTrue(limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 2))
    }

    @Test
    fun `blocked vibrations do not count against the limits`() {
        limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3)
        clock.advanceBy(1.minutes)
        limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3)
        clock.advanceBy(4.minutes)

        assertTrue(limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3))
    }

    @Test
    fun `start events and breathing are always allowed`() {
        limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 1)

        assertTrue(limiter.tryAcquire(HapticEvent.BLOCK_START, maxPerHour = 1))
        assertTrue(limiter.tryAcquire(HapticEvent.BREATHE, maxPerHour = 1))
    }

    @Test
    fun `exempt events do not use up the interval`() {
        limiter.tryAcquire(HapticEvent.BLOCK_START, maxPerHour = 3)

        assertTrue(limiter.tryAcquire(HapticEvent.PAUSE_SUGGESTED, maxPerHour = 3))
    }
}
