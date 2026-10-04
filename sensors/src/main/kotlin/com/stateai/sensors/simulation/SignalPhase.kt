package com.stateai.sensors.simulation

import kotlin.time.Duration

/**
 * A stretch of simulated signal. Heart rate ramps linearly from [startHeartRate] to [endHeartRate]
 * with gaussian jitter; movement stays around [movement] with occasional fidget spikes.
 */
data class SignalPhase(
    val duration: Duration,
    val startHeartRate: Double,
    val endHeartRate: Double,
    val heartRateJitter: Double,
    val movement: Double,
    val fidgetChancePerSecond: Double,
) {
    init {
        require(duration.isPositive()) { "Phase duration must be positive" }
        require(fidgetChancePerSecond in 0.0..1.0) { "Fidget chance is a probability" }
    }
}
