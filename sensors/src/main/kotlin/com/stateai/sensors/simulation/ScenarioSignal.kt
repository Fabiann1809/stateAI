package com.stateai.sensors.simulation

import java.util.Random

/** Heart rate and movement of one simulated second. */
data class SignalValue(val heartRateBpm: Double, val movement: Double)

/**
 * Deterministic signal for a scenario: the value of a given second depends only on the scenario,
 * the [seed] and the second, so runs are reproducible and any second can be computed directly.
 */
class ScenarioSignal(private val scenario: Scenario, private val seed: Long = DEFAULT_SEED) {
    fun valueAt(second: Long): SignalValue {
        val (phase, progress) = scenario.phaseAt(second)
        val random = Random(seed * SEED_MULTIPLIER + second)
        val heartRate = phase.startHeartRate + (phase.endHeartRate - phase.startHeartRate) * progress +
            random.nextGaussian() * phase.heartRateJitter
        val baseMovement = (phase.movement + random.nextGaussian() * phase.movement * MOVEMENT_NOISE_RATIO)
        val fidget = if (random.nextDouble() < phase.fidgetChancePerSecond) FIDGET_MAGNITUDE else 0.0
        return SignalValue(
            heartRateBpm = heartRate.coerceAtLeast(MIN_HEART_RATE),
            movement = (baseMovement + fidget).coerceAtLeast(0.0),
        )
    }

    companion object {
        const val DEFAULT_SEED = 42L
        const val FIDGET_MAGNITUDE = 2.5
        private const val SEED_MULTIPLIER = 1_000_003L
        private const val MOVEMENT_NOISE_RATIO = 0.2
        private const val MIN_HEART_RATE = 30.0
    }
}
