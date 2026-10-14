package com.stateai.domain.segment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FeedbackTest {
    @Test
    fun `ratings go from 1 to 5 in order`() {
        assertEquals(listOf(1, 2, 3, 4, 5), Feedback.entries.map { it.rating })
    }

    @Test
    fun `a rating maps back to its feedback`() {
        Feedback.entries.forEach { assertEquals(it, Feedback.fromRating(it.rating)) }
        assertNull(Feedback.fromRating(0))
        assertNull(Feedback.fromRating(6))
    }

    @Test
    fun `only 1 and 2 are negative and only 4 and 5 are positive`() {
        assertEquals(listOf(Feedback.VERY_BAD, Feedback.BAD), Feedback.entries.filter { it.isNegative })
        assertEquals(listOf(Feedback.GOOD, Feedback.VERY_GOOD), Feedback.entries.filter { it.isPositive })
        assertFalse(Feedback.OKAY.isNegative || Feedback.OKAY.isPositive)
    }

    @Test
    fun `the names stored before the 5-point scale keep their meaning`() {
        assertEquals(2, Feedback.valueOf("BAD").rating)
        assertEquals(3, Feedback.valueOf("OKAY").rating)
        assertTrue(Feedback.valueOf("GOOD").isPositive)
    }
}
