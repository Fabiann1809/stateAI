package com.stateai.domain.state

import com.stateai.domain.profile.MovementLevel
import com.stateai.domain.profile.Sensitivity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Thresholds of the rule engine. Heart rate deltas are in bpm above the resting baseline and are
 * scaled by the category sensitivity; movement limits are mean linear acceleration in m/s².
 */
data class ClassifierThresholds(
    val lowMaxHeartRateDelta: Double = 5.0,
    val highMinHeartRateDelta: Double = 12.0,
    val restlessMinFidgets: Int = 5,
    val restlessMinElapsed: Duration = 15.minutes,
    val sensitivityFactors: Map<Sensitivity, Double> = DEFAULT_SENSITIVITY_FACTORS,
    val movementLimits: Map<MovementLevel, Double> = DEFAULT_MOVEMENT_LIMITS,
) {
    fun factorFor(sensitivity: Sensitivity): Double = sensitivityFactors.getValue(sensitivity)

    fun movementLimitFor(level: MovementLevel): Double = movementLimits.getValue(level)

    companion object {
        private const val LESS_REACTIVE = 1.25
        private const val NEUTRAL = 1.0
        private const val MORE_REACTIVE = 0.8
        private const val VERY_LOW_MOVEMENT_LIMIT = 0.15
        private const val LOW_MOVEMENT_LIMIT = 0.25
        private const val MEDIUM_MOVEMENT_LIMIT = 0.45
        private const val MEDIUM_HIGH_MOVEMENT_LIMIT = 0.8

        /** Lower sensitivity widens the thresholds; higher sensitivity narrows them. */
        val DEFAULT_SENSITIVITY_FACTORS = mapOf(
            Sensitivity.LOW to LESS_REACTIVE,
            Sensitivity.MEDIUM to NEUTRAL,
            Sensitivity.HIGH to MORE_REACTIVE,
        )

        val DEFAULT_MOVEMENT_LIMITS = mapOf(
            MovementLevel.VERY_LOW to VERY_LOW_MOVEMENT_LIMIT,
            MovementLevel.LOW to LOW_MOVEMENT_LIMIT,
            MovementLevel.MEDIUM to MEDIUM_MOVEMENT_LIMIT,
            MovementLevel.MEDIUM_HIGH to MEDIUM_HIGH_MOVEMENT_LIMIT,
        )
    }
}
