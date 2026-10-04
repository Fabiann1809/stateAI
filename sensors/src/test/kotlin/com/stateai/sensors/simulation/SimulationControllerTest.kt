package com.stateai.sensors.simulation

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulationControllerTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-04T09:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `starts with the initial scenario`() {
        assertEquals(ScenarioId.FATIGUE, SimulationController(initial = ScenarioId.FATIGUE).scenario.value.id)
    }

    @Test
    fun `changing the scenario changes the live signal`() = runTest {
        val controller = SimulationController(initial = ScenarioId.DEEP_FOCUS)
        val source = SimulatedSensorSource(controller.scenario, clock)
        val heartRates = mutableListOf<Double>()
        val collector = launch { source.samples().collect { heartRates += it.heartRateBpm!! } }

        advanceTimeBy(60_000)
        controller.select(ScenarioId.OVERLOAD)
        advanceTimeBy(1_200_000)
        collector.cancel()

        val focusMean = heartRates.take(60).average()
        val overloadEnd = heartRates.takeLast(120).average()
        assertTrue(overloadEnd > focusMean + 8, "focus=$focusMean overload=$overloadEnd")
    }

    @Test
    fun `selecting the same scenario keeps it`() = runTest {
        val controller = SimulationController(initial = ScenarioId.MIXED)
        val before = controller.scenario.first()

        controller.select(ScenarioId.MIXED)

        assertTrue(before === controller.scenario.value)
    }
}
