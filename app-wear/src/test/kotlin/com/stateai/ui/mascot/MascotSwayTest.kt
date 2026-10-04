package com.stateai.ui.mascot

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MascotSwayTest {
    @Test
    fun `sways only on a live screen with animations allowed`() {
        assertTrue(shouldSway(isAmbient = false, reduceMotion = false))
        assertFalse(shouldSway(isAmbient = true, reduceMotion = false))
        assertFalse(shouldSway(isAmbient = false, reduceMotion = true))
    }
}
