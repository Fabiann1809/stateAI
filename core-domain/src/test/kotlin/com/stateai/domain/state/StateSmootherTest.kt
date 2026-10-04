package com.stateai.domain.state

import com.stateai.domain.state.ActivationLevel.HIGH
import com.stateai.domain.state.ActivationLevel.LOW
import com.stateai.domain.state.ActivationLevel.MEDIUM
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StateSmootherTest {
    private val smoother = StateSmoother()

    @Test
    fun `accepts the first estimate immediately`() {
        assertEquals(HIGH, levels(HIGH).last())
    }

    @Test
    fun `a single different window does not change the level`() {
        assertEquals(listOf(LOW, LOW, LOW), levels(LOW, HIGH, LOW))
    }

    @Test
    fun `two consecutive equal windows change the level`() {
        assertEquals(listOf(LOW, LOW, HIGH), levels(LOW, HIGH, HIGH))
    }

    @Test
    fun `alternating candidates never switch`() {
        assertEquals(listOf(LOW, LOW, LOW, LOW), levels(LOW, HIGH, MEDIUM, HIGH))
    }

    @Test
    fun `restlessness is debounced too`() {
        val flags = listOf(false, true, false, true, true).map { smoother.update(StateEstimate(LOW, it)).restless }

        assertEquals(listOf(false, false, false, false, true), flags)
    }

    private fun levels(vararg raw: ActivationLevel) = raw.map { smoother.update(StateEstimate(it, false)).level }
}
