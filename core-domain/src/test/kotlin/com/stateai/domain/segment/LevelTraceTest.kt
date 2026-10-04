package com.stateai.domain.segment

import com.stateai.domain.state.ActivationLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LevelTraceTest {
    @Test
    fun `appends one symbol per window`() {
        val trace = LevelTrace().plus(ActivationLevel.LOW).plus(null).plus(ActivationLevel.HIGH)

        assertEquals("L-H", trace.symbols)
        assertEquals(listOf(ActivationLevel.LOW, null, ActivationLevel.HIGH), trace.levels())
    }

    @Test
    fun `counts clean windows`() {
        val trace = LevelTrace("LL-M-")

        assertEquals(5, trace.windowCount)
        assertEquals(3, trace.cleanWindowCount)
    }

    @Test
    fun `rejects unknown symbols`() {
        assertThrows<IllegalArgumentException> { LevelTrace("LX") }
    }
}
