package com.stateai.domain.mascot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MascotNameTest {
    @Test
    fun `trims and collapses spaces`() {
        assertEquals("Lumi la llama", MascotName.clean("  Lumi   la\tllama "))
    }

    @Test
    fun `long names are cut to fit the watch`() {
        assertEquals("Una llamita muy", MascotName.clean("Una llamita muy turquesa"))
    }

    @Test
    fun `blank stays empty`() {
        assertEquals("", MascotName.clean("   "))
    }
}
