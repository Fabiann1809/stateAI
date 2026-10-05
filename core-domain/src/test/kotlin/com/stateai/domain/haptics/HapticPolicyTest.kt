package com.stateai.domain.haptics

import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.ActivationLevel.HIGH
import com.stateai.domain.state.ActivationLevel.LOW
import com.stateai.domain.state.ActivationLevel.MEDIUM
import com.stateai.domain.state.StateEstimate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HapticPolicyTest {
    private val policy = HapticPolicy()

    @Test
    fun `focus is never interrupted`() {
        assertEquals(List(30) { null }, feed(List(30) { LOW }))
    }

    @Test
    fun `sustained overload alerts once after three windows`() {
        val events = feed(listOf(MEDIUM, HIGH, HIGH, HIGH, HIGH, HIGH))

        assertEquals(listOf(null, null, null, HapticEvent.OVERLOAD_ALERT, null, null), events)
    }

    @Test
    fun `brief overload does not alert`() {
        assertEquals(List(5) { null }, feed(listOf(HIGH, HIGH, MEDIUM, HIGH, HIGH)))
    }

    @Test
    fun `a new overload episode alerts again`() {
        val events = feed(listOf(HIGH, HIGH, HIGH, MEDIUM, HIGH, HIGH, HIGH))

        assertEquals(2, events.count { it == HapticEvent.OVERLOAD_ALERT })
    }

    @Test
    fun `sustained restlessness during focus suggests a pause once`() {
        val events = List(6) { policy.onEstimate(StateEstimate(LOW, restless = true))?.event }

        assertEquals(listOf(null, null, HapticEvent.PAUSE_SUGGESTED, null, null, null), events)
    }

    @Test
    fun `leaving a long focus stretch suggests a pause`() {
        val events = feed(List(10) { LOW } + MEDIUM)

        assertEquals(HapticEvent.PAUSE_SUGGESTED, events.last())
    }

    @Test
    fun `leaving a short focus stretch does not`() {
        assertEquals(null, feed(List(4) { LOW } + MEDIUM).last())
    }

    private fun feed(levels: List<ActivationLevel>) = levels.map { policy.onEstimate(StateEstimate(it, false))?.event }
}
