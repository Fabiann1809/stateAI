package com.stateai.sensors.simulation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScenariosTest {
    private val resting = Scenarios.DEFAULT_RESTING_HEART_RATE
    private val scenarios = Scenarios(resting)

    @Test
    fun `deep focus stays close to resting heart rate with little movement`() {
        val signal = signalOf(ScenarioId.DEEP_FOCUS)

        assertEquals(resting + 2, signal.meanHeartRate(0, 30), 1.0)
        assertTrue(signal.meanMovement(0, 30) < 0.15)
    }

    @Test
    fun `overload heart rate climbs well above the start`() {
        val signal = signalOf(ScenarioId.OVERLOAD)

        assertTrue(signal.meanHeartRate(15, 20) > signal.meanHeartRate(0, 5) + 8)
    }

    @Test
    fun `fatigue brings far more fidgeting at the end than at the start`() {
        val signal = signalOf(ScenarioId.FATIGUE)

        assertTrue(signal.fidgets(30, 40) > signal.fidgets(0, 10) * 3)
    }

    @Test
    fun `mixed session rises into overload and recovers`() {
        val signal = signalOf(ScenarioId.MIXED)
        val calm = signal.meanHeartRate(0, 10)
        val peak = signal.meanHeartRate(16, 18)
        val recovered = signal.meanHeartRate(22, 23)

        assertTrue(peak > calm + 10)
        assertTrue(recovered < peak - 8)
    }

    @Test
    fun `every scenario has a positive duration`() {
        ScenarioId.entries.forEach { assertTrue(scenarios.byId(it).totalDuration.isPositive()) }
    }

    private fun signalOf(id: ScenarioId) = ScenarioSignal(scenarios.byId(id))

    private fun ScenarioSignal.values(fromMinute: Int, toMinute: Int) =
        (fromMinute * 60L until toMinute * 60L).map(::valueAt)

    private fun ScenarioSignal.meanHeartRate(fromMinute: Int, toMinute: Int) =
        values(fromMinute, toMinute).map { it.heartRateBpm }.average()

    private fun ScenarioSignal.meanMovement(fromMinute: Int, toMinute: Int) =
        values(fromMinute, toMinute).map { it.movement }.average()

    private fun ScenarioSignal.fidgets(fromMinute: Int, toMinute: Int) =
        values(fromMinute, toMinute).count { it.movement > 2.0 }
}
