package com.stateai.domain.activity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ActivityNameTest {
    @Test
    fun `lowercases and trims`() {
        assertEquals("bd", ActivityName.of("  BD ")?.value)
    }

    @Test
    fun `removes accents`() {
        assertEquals("programacion", ActivityName.of("Programación")?.value)
    }

    @Test
    fun `collapses inner whitespace`() {
        assertEquals("tesis capitulo 2", ActivityName.of("Tesis   capítulo\t2")?.value)
    }

    @Test
    fun `names differing only in case and spaces are equal`() {
        assertEquals(ActivityName.of("BD"), ActivityName.of("bd "))
    }

    @Test
    fun `blank text has no name`() {
        assertNull(ActivityName.of("   "))
    }
}
