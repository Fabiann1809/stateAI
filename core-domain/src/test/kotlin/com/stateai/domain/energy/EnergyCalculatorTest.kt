package com.stateai.domain.energy

import com.stateai.domain.learning.FocusProfile
import com.stateai.domain.learning.SlotStats
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import com.stateai.domain.testing.testSegment
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EnergyCalculatorTest {
    private val calculator = EnergyCalculator(ZoneOffset.UTC)
    private val start = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `focus consumes slowly`() {
        val budget = calculator.compute(listOf(session("L".repeat(60))), FocusProfile.EMPTY)

        assertEquals(6.0, budget.consumed, 1e-9)
        assertEquals(94.0, budget.level, 1e-9)
    }

    @Test
    fun `overload consumes three times faster than focus`() {
        val budget = calculator.compute(listOf(session("H".repeat(60))), FocusProfile.EMPTY)

        assertEquals(18.0, budget.consumed, 1e-9)
    }

    @Test
    fun `restless time costs extra`() {
        val restless = session("L".repeat(60)).copy(restlessTime = 20.minutes)

        assertEquals(8.0, calculator.compute(listOf(restless), FocusProfile.EMPTY).consumed, 1e-9)
    }

    @Test
    fun `a pause that improved the state recovers energy`() {
        val helped = pause(ActivationLevel.HIGH, ActivationLevel.LOW)
        val useless = pause(ActivationLevel.LOW, ActivationLevel.LOW)
        val segment = session("H".repeat(60)).copy(pauses = listOf(helped, useless))

        val budget = calculator.compute(listOf(segment), FocusProfile.EMPTY)

        assertEquals(6.0, budget.recovered, 1e-9)
        assertEquals(88.0, budget.level, 1e-9)
    }

    @Test
    fun `work in a good hour costs less than in a bad hour`() {
        val profile = FocusProfile(
            bySlot = emptyMap(),
            byHour = mapOf(9 to SlotStats(0.9, 300, 10), 15 to SlotStats(0.3, 300, 10)),
            overallFocusShare = 0.6,
        )
        val morning = calculator.compute(listOf(session("M".repeat(50))), profile)
        val afternoon = calculator.compute(listOf(session("M".repeat(50), at = "2026-10-04T15:00:00Z")), profile)

        assertEquals(4.8, morning.consumed, 1e-9)
        assertEquals(7.2, afternoon.consumed, 1e-9)
    }

    @Test
    fun `a long loaded day ends with low energy and never below zero`() {
        val day = (0 until 8).map { session("H".repeat(60), at = "2026-10-04T0${1 + it}:00:00Z") }

        val budget = calculator.compute(day, FocusProfile.EMPTY)

        assertTrue(budget.isLow)
        assertEquals(0.0, budget.level, 1e-9)
    }

    @Test
    fun `a fresh day is not low`() {
        assertFalse(calculator.compute(emptyList(), FocusProfile.EMPTY).isLow)
    }

    private fun pause(before: ActivationLevel, after: ActivationLevel) =
        PauseRecord(start, start, StateEstimate(before, false), StateEstimate(after, false))

    private fun session(trace: String, at: String = "2026-10-04T09:00:00Z") =
        testSegment(start = Instant.parse(at), duration = trace.length.minutes, trace = LevelTrace(trace))
}
