package com.stateai.domain.features

import java.time.Instant

/** Features of one window of samples. Definitions in `docs/features.md`. */
data class FeatureWindow(
    val start: Instant,
    val end: Instant,
    val sampleCount: Int,
    val heartRateCount: Int,
    val meanHeartRate: Double?,
    val heartRateStdDev: Double,
    val heartRateMeanAbsDiff: Double,
    val meanMovement: Double,
    val fidgetCount: Int,
    val highMovementShare: Double,
) {
    val heartRateCoverage: Double get() = if (sampleCount == 0) 0.0 else heartRateCount.toDouble() / sampleCount
}
