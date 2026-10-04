package com.stateai.domain.session

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SessionProgressTest {
    @Test
    fun `fraction is the share of the target completed`() {
        assertEquals(0.5f, SessionProgress(elapsed = 20.minutes, target = 40.minutes).fraction)
    }

    @Test
    fun `fraction is capped at one after the target`() {
        assertEquals(1f, SessionProgress(elapsed = 60.minutes, target = 40.minutes).fraction)
    }

    @Test
    fun `target is reached exactly at the target time`() {
        assertTrue(SessionProgress(elapsed = 40.minutes, target = 40.minutes).isTargetReached)
        assertFalse(SessionProgress(elapsed = 39.minutes, target = 40.minutes).isTargetReached)
    }

    @Test
    fun `zero target counts as complete`() {
        assertEquals(1f, SessionProgress(elapsed = Duration.ZERO, target = Duration.ZERO).fraction)
    }
}
