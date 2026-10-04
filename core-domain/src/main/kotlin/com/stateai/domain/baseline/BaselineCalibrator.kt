package com.stateai.domain.baseline

import com.stateai.domain.features.FeatureExtractor
import com.stateai.domain.sensing.SensorSample
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/** Settings of the initial resting calibration (SPEC 6.4). */
data class CalibrationConfig(
    val duration: Duration = 2.minutes,
    val minHeartRateSamples: Int = 60,
    val maxRestingMovement: Double = 1.0,
)

/** Progress of the calibration: still collecting, or finished with a baseline. */
sealed interface CalibrationProgress {
    data class Collecting(val fraction: Float) : CalibrationProgress

    data class Done(val baseline: UserBaseline) : CalibrationProgress
}

/**
 * Builds the personal baseline from about two minutes of still samples. Samples with movement are
 * ignored; if too few heart rates arrived when the time is up, it keeps collecting.
 */
class BaselineCalibrator(
    private val config: CalibrationConfig = CalibrationConfig(),
    private val extractor: FeatureExtractor = FeatureExtractor(),
) {
    private val samples = mutableListOf<SensorSample>()

    fun add(sample: SensorSample): CalibrationProgress {
        if (sample.movement <= config.maxRestingMovement) samples += sample
        val first = samples.firstOrNull() ?: return CalibrationProgress.Collecting(0f)
        val elapsed = java.time.Duration.between(first.timestamp, sample.timestamp)
        val heartRates = samples.count { it.heartRateBpm != null }
        val timeFraction = elapsed.toMillis().toFloat() / config.duration.toJavaDuration().toMillis()
        val enoughData = timeFraction >= 1f && heartRates >= config.minHeartRateSamples
        return if (enoughData) {
            CalibrationProgress.Done(baselineAt(sample))
        } else {
            CalibrationProgress.Collecting(
                timeFraction.coerceAtMost(1f),
            )
        }
    }

    private fun baselineAt(last: SensorSample): UserBaseline {
        val window = requireNotNull(extractor.extract(samples))
        return UserBaseline(
            restingHeartRate = requireNotNull(window.meanHeartRate),
            heartRateMeanAbsDiff = window.heartRateMeanAbsDiff,
            updatedAt = last.timestamp,
        )
    }
}
