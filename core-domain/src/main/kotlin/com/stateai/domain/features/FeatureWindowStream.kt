package com.stateai.domain.features

import com.stateai.domain.sensing.SensorSample
import kotlin.time.toJavaDuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Turns a stream of samples into feature windows: every [FeatureConfig.step] it emits the features
 * of the last [FeatureConfig.windowLength] of samples. The first window comes after one step.
 */
class FeatureWindowStream(
    private val extractor: FeatureExtractor = FeatureExtractor(),
    private val config: FeatureConfig = FeatureConfig(),
) {
    fun windows(samples: Flow<SensorSample>): Flow<FeatureWindow> = flow {
        val buffer = ArrayDeque<SensorSample>()
        var nextEmission: java.time.Instant? = null
        samples.collect { sample ->
            buffer.addLast(sample)
            val windowStart = sample.timestamp.minus(config.windowLength.toJavaDuration())
            while (buffer.first().timestamp <= windowStart) buffer.removeFirst()

            val due = nextEmission ?: sample.timestamp.plus(config.step.toJavaDuration()).also { nextEmission = it }
            if (sample.timestamp >= due) {
                extractor.extract(buffer.toList())?.let { emit(it) }
                nextEmission = due.plus(config.step.toJavaDuration())
            }
        }
    }
}
