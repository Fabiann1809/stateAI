package com.stateai.domain.energy

/**
 * Rates of the estimated energy indicator (SPEC 6.5), in points per minute of session unless noted.
 * The values are product choices, not physiology: they make a full working day of mixed load end
 * around a quarter of the battery.
 */
data class EnergyConfig(
    val dayStart: Double = 100.0,
    val focusRate: Double = 0.10,
    val normalRate: Double = 0.12,
    val overloadRate: Double = 0.30,
    val unknownRate: Double = 0.10,
    val restlessExtraRate: Double = 0.10,
    /** Points recovered by a guided pause that improved the state. */
    val usefulPauseRecovery: Double = 6.0,
    /** Consumption multiplier in learned good / bad focus hours. */
    val goodHourFactor: Double = 0.8,
    val badHourFactor: Double = 1.2,
    /** How far from the person's average an hour must be to count as good or bad. */
    val hourMargin: Double = 0.1,
    val lowThreshold: Double = 25.0,
)

/** Estimated energy left today, from 0 to 100. Never real biological energy. */
data class EnergyBudget(val level: Double, val consumed: Double, val recovered: Double, val isLow: Boolean)
