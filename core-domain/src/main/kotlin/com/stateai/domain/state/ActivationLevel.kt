package com.stateai.domain.state

/**
 * Estimated activation relative to the personal baseline (SPEC 6.4.1). `LOW` during a session
 * counts as focus time. These are estimates from heart rate and movement, not measurements.
 */
enum class ActivationLevel {
    LOW,
    MEDIUM,
    HIGH,
}

/** Result of classifying one window. */
data class StateEstimate(val level: ActivationLevel, val restless: Boolean)
