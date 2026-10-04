package com.stateai.domain.activity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ActivityNameTest {
    @Test
    fun `lowercases and trims`() {
        assertEquals("bd", ActivityName.of("  BD ")?.normalized)
    }

    @Test
    fun `removes accents`() {
        assertEquals("programacion", ActivityName.of("Programación")?.normalized)
    }

    @Test
    fun `collapses inner whitespace`() {
        assertEquals("tesis capitulo 2", ActivityName.of("Tesis   capítulo\t2")?.normalized)
    }

    @Test
    fun `names differing only in case and spaces are equal`() {
        assertEquals(ActivityName.of("BD"), ActivityName.of("bd "))
    }

    @Test
    fun `display keeps accents and case but trims spaces`() {
        assertEquals("Tesis capítulo 2", ActivityName.of("  Tesis   capítulo 2 ")?.display)
    }

    @Test
    fun `blank text has no name`() {
        assertNull(ActivityName.of("   "))
    }
}
