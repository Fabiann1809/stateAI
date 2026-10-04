package com.stateai.domain.features

import com.stateai.domain.sensing.SensorSample
import kotlin.math.abs
import kotlin.math.sqrt

/** Computes the features of a window of samples, as defined in `docs/features.md`. */
class FeatureExtractor(private val config: FeatureConfig = FeatureConfig()) {
    /** Returns null for an empty window. Samples must be in chronological order. */
    fun extract(samples: List<SensorSample>): FeatureWindow? {
        if (samples.isEmpty()) return null
        val heartRates = samples.mapNotNull { it.heartRateBpm }
        val movements = samples.map { it.movement }
        return FeatureWindow(
            start = samples.first().timestamp,
            end = samples.last().timestamp,
            sampleCount = samples.size,
            heartRateCount = heartRates.size,
            meanHeartRate = heartRates.takeIf { it.isNotEmpty() }?.average(),
            heartRateStdDev = populationStdDev(heartRates),
            heartRateMeanAbsDiff = meanAbsSuccessiveDiff(samples),
            meanMovement = movements.average(),
            fidgetCount = countRisingEdges(movements, config.fidgetThreshold),
            highMovementShare = movements.count { it > config.highMovementThreshold }.toDouble() / samples.size,
        )
    }

    /** Whether the window carries reliable evidence (low sustained movement, enough heart rate). */
    fun isClean(window: FeatureWindow): Boolean = window.highMovementShare < config.maxHighMovementShare &&
        window.heartRateCoverage >= config.minHeartRateCoverage

    private fun populationStdDev(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        val mean = values.average()
        return sqrt(values.sumOf { (it - mean) * (it - mean) } / values.size)
    }

    private fun meanAbsSuccessiveDiff(samples: List<SensorSample>): Double {
        val diffs = samples.zipWithNext().mapNotNull { (previous, current) ->
            val a = previous.heartRateBpm
            val b = current.heartRateBpm
            if (a != null && b != null) abs(b - a) else null
        }
        return if (diffs.isEmpty()) 0.0 else diffs.average()
    }

    private fun countRisingEdges(values: List<Double>, threshold: Double): Int {
        var previousAbove = false
        var count = 0
        for (value in values) {
            val above = value > threshold
            if (above && !previousAbove) count++
            previousAbove = above
        }
        return count
    }
}
