package com.stateai.domain.learning

import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.testing.testSegment
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidSessionRuleTest {
    private val rule = ValidSessionRule()

    @Test
    fun `a long clean session is valid`() {
        assertTrue(rule.isValid(testSegment(duration = 30.minutes)))
    }

    @Test
    fun `a short session is not valid`() {
        assertFalse(rule.isValid(testSegment(duration = 9.minutes)))
    }

    @Test
    fun `a noisy session is not valid`() {
        val noisy = testSegment(duration = 20.minutes, trace = LevelTrace("LLLLLLLL" + "-".repeat(12)))

        assertFalse(rule.isValid(noisy))
    }

    @Test
    fun `exactly sixty percent clean windows is enough`() {
        val borderline = testSegment(duration = 20.minutes, trace = LevelTrace("L".repeat(12) + "-".repeat(8)))

        assertTrue(rule.isValid(borderline))
    }

    @Test
    fun `a session without windows is not valid`() {
        assertFalse(rule.isValid(testSegment(duration = 20.minutes, trace = LevelTrace(""))))
    }
}
