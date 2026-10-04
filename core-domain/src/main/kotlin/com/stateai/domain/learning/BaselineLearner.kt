package com.stateai.domain.learning

import com.stateai.domain.baseline.BaselineKeeper
import com.stateai.domain.segment.Segment

/**
 * Refines the personal baseline after each session with an exponential moving average of the
 * heart rate in calm windows: `new = alpha * calm + (1 - alpha) * old`.
 */
class BaselineLearner(private val keeper: BaselineKeeper, private val alpha: Double = DEFAULT_ALPHA) :
    SegmentLearner {
    init {
        require(alpha in 0.0..1.0) { "alpha is a weight between 0 and 1" }
    }

    override suspend fun learn(segment: Segment) {
        val calm = segment.calmHeartRate ?: return
        keeper.refine { baseline ->
            baseline.copy(
                restingHeartRate = alpha * calm + (1 - alpha) * baseline.restingHeartRate,
                updatedAt = segment.end,
            )
        }
    }

    companion object {
        const val DEFAULT_ALPHA = 0.2
    }
}
