package com.stateai.domain.features

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Window timing and movement thresholds of the feature computation (`docs/features.md`). */
data class FeatureConfig(
    val windowLength: Duration = 3.minutes,
    val step: Duration = 60.seconds,
    val fidgetThreshold: Double = 1.5,
    val highMovementThreshold: Double = 1.0,
    val maxHighMovementShare: Double = 0.3,
    val minHeartRateCoverage: Double = 0.5,
)
