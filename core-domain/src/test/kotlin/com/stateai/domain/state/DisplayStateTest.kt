package com.stateai.domain.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DisplayStateTest {
    @Test
    fun `levels map to their display state`() {
        assertEquals(DisplayState.FOCUSED, DisplayState.of(StateEstimate(ActivationLevel.LOW, false), false))
        assertEquals(DisplayState.NORMAL, DisplayState.of(StateEstimate(ActivationLevel.MEDIUM, false), false))
        assertEquals(DisplayState.OVERLOADED, DisplayState.of(StateEstimate(ActivationLevel.HIGH, false), false))
    }

    @Test
    fun `overload with restlessness or low energy is shown as exhausted`() {
        assertEquals(DisplayState.EXHAUSTED, DisplayState.of(StateEstimate(ActivationLevel.HIGH, true), false))
        assertEquals(DisplayState.EXHAUSTED, DisplayState.of(StateEstimate(ActivationLevel.HIGH, false), true))
    }

    @Test
    fun `restlessness or low energy alone never means exhausted`() {
        assertEquals(DisplayState.FOCUSED, DisplayState.of(StateEstimate(ActivationLevel.LOW, true), true))
        assertEquals(DisplayState.NORMAL, DisplayState.of(StateEstimate(ActivationLevel.MEDIUM, true), true))
    }
}
