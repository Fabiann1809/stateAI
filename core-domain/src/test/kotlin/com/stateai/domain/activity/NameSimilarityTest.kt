package com.stateai.domain.activity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NameSimilarityTest {
    @Test
    fun `case and accents do not matter`() {
        assertEquals(1.0, NameSimilarity.score(name("Física"), name("fisica")))
    }

    @Test
    fun `small typos are still the same activity`() {
        assertTrue(NameSimilarity.areSimilar(name("bases de datos"), name("baces de datos")))
    }

    @Test
    fun `a name contained word by word in another is similar`() {
        assertTrue(NameSimilarity.areSimilar(name("informe"), name("informe trimestral")))
    }

    @Test
    fun `different names are not similar`() {
        assertFalse(NameSimilarity.areSimilar(name("tesis"), name("novela")))
        assertFalse(NameSimilarity.areSimilar(name("BD"), name("bases de datos")))
    }

    @Test
    fun `very short names are not matched by containment`() {
        assertFalse(NameSimilarity.areSimilar(name("ia"), name("ia y datos")))
    }

    private fun name(text: String) = requireNotNull(ActivityName.of(text))
}
