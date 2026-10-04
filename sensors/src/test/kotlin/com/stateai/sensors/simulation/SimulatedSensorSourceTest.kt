package com.stateai.sensors.simulation

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedSensorSourceTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-04T09:00:00Z"), ZoneOffset.UTC)
    private val calm = scenario(ScenarioId.DEEP_FOCUS, heartRate = 60.0)
    private val busy = scenario(ScenarioId.OVERLOAD, heartRate = 100.0)

    @Test
    fun `emits one sample per second`() = runTest {
        val source = SimulatedSensorSource(MutableStateFlow(calm), clock)

        source.samples().take(10).toList()

        assertEquals(9_000L, currentTime)
    }

    @Test
    fun `samples follow the scenario`() = runTest {
        val samples = SimulatedSensorSource(MutableStateFlow(busy), clock).samples().take(30).toList()

        assertEquals(100.0, samples.mapNotNull { it.heartRateBpm }.average(), 2.0)
    }

    @Test
    fun `same seed reproduces the same signal`() {
        val first = ScenarioSignal(calm, seed = 7).valueAt(123)
        val second = ScenarioSignal(calm, seed = 7).valueAt(123)

        assertEquals(first, second)
    }

    @Test
    fun `different seeds give different signals`() {
        assertNotEquals(ScenarioSignal(calm, seed = 1).valueAt(5), ScenarioSignal(calm, seed = 2).valueAt(5))
    }

    @Test
    fun `scenario loops after its total duration`() {
        val signal = ScenarioSignal(calm)
        val total = calm.totalDuration.inWholeSeconds

        assertEquals(calm.phaseAt(3).first, calm.phaseAt(total + 3).first)
        assertNotEquals(signal.valueAt(3), signal.valueAt(total + 3))
    }

    private fun scenario(id: ScenarioId, heartRate: Double) = Scenario(
        id,
        listOf(SignalPhase(10.minutes, heartRate, heartRate, 1.0, movement = 0.1, fidgetChancePerSecond = 0.0)),
    )
}
