package com.stateai.sensors.simulation

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Scripted scenarios relative to a resting heart rate. Offsets are in bpm above [restingHeartRate];
 * the values are illustrative, not physiological ground truth.
 */
class Scenarios(private val restingHeartRate: Double = DEFAULT_RESTING_HEART_RATE) {
    fun byId(id: ScenarioId): Scenario = when (id) {
        ScenarioId.DEEP_FOCUS -> Scenario(id, listOf(focus(30.minutes)))
        ScenarioId.OVERLOAD -> Scenario(id, listOf(focus(5.minutes), overloadRamp(15.minutes)))
        ScenarioId.FATIGUE -> Scenario(id, listOf(focus(20.minutes), restless(20.minutes)))
        ScenarioId.MIXED -> Scenario(
            id,
            listOf(focus(10.minutes), overloadRamp(8.minutes), recovery(5.minutes), restless(7.minutes)),
        )
    }

    private fun focus(duration: Duration) = phase(duration, FOCUS_OFFSET, FOCUS_OFFSET, CALM_JITTER, STILL, RARE)

    private fun overloadRamp(duration: Duration) =
        phase(duration, OVERLOAD_START_OFFSET, OVERLOAD_PEAK_OFFSET, LOADED_JITTER, LIGHT_MOVEMENT, OCCASIONAL)

    private fun recovery(duration: Duration) =
        phase(duration, OVERLOAD_PEAK_OFFSET, FOCUS_OFFSET, LOADED_JITTER, STILL, RARE)

    private fun restless(duration: Duration) =
        phase(duration, RESTLESS_OFFSET, RESTLESS_OFFSET, LOADED_JITTER, LIGHT_MOVEMENT, FREQUENT)

    @Suppress("LongParameterList")
    private fun phase(
        duration: Duration,
        startOffset: Double,
        endOffset: Double,
        jitter: Double,
        movement: Double,
        fidgetChance: Double,
    ) = SignalPhase(
        duration = duration,
        startHeartRate = restingHeartRate + startOffset,
        endHeartRate = restingHeartRate + endOffset,
        heartRateJitter = jitter,
        movement = movement,
        fidgetChancePerSecond = fidgetChance,
    )

    companion object {
        const val DEFAULT_RESTING_HEART_RATE = 65.0
        private const val FOCUS_OFFSET = 2.0
        private const val OVERLOAD_START_OFFSET = 4.0
        private const val OVERLOAD_PEAK_OFFSET = 18.0
        private const val RESTLESS_OFFSET = 5.0
        private const val CALM_JITTER = 1.0
        private const val LOADED_JITTER = 2.5
        private const val STILL = 0.05
        private const val LIGHT_MOVEMENT = 0.15
        private const val RARE = 0.005
        private const val OCCASIONAL = 0.02
        private const val FREQUENT = 0.08
    }
}
