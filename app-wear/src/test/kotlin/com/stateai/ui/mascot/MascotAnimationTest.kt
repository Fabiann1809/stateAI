package com.stateai.ui.mascot

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MascotAnimationTest {
    @Test
    fun `moves only on a live screen with animations allowed`() {
        assertTrue(shouldAnimate(isAmbient = false, reduceMotion = false))
        assertFalse(shouldAnimate(isAmbient = true, reduceMotion = false))
        assertFalse(shouldAnimate(isAmbient = false, reduceMotion = true))
    }
}
