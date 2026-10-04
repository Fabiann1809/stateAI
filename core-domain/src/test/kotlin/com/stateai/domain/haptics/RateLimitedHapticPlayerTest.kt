package com.stateai.domain.haptics

import com.stateai.domain.testing.MutableClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RateLimitedHapticPlayerTest {
    private val played = mutableListOf<HapticEvent>()
    private val player = RateLimitedHapticPlayer(
        delegate = { played += it },
        limiter = HapticRateLimiter(MutableClock()),
        maxPerHour = { 3 },
    )

    @Test
    fun `forwards allowed events and drops blocked ones`() {
        player.play(HapticEvent.PAUSE_SUGGESTED)
        player.play(HapticEvent.OVERLOAD_ALERT)
        player.play(HapticEvent.BLOCK_START)

        assertEquals(listOf(HapticEvent.PAUSE_SUGGESTED, HapticEvent.BLOCK_START), played)
    }
}
