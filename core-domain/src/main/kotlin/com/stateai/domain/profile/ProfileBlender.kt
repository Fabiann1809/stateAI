package com.stateai.domain.profile

import com.stateai.domain.learning.ActivityLearning
import com.stateai.domain.state.ClassifierThresholds

/**
 * Blends an activity's learning into its category profile (SPEC 6.7) so behavior changes gradually:
 * `own = n / (n + K)` and `value = own * learned + (1 - own) * category` for the target block and the
 * movement limit. The learned movement limit is the activity's mean movement times [movementMargin].
 */
class ProfileBlender(
    private val k: Int = DEFAULT_K,
    private val movementMargin: Double = DEFAULT_MOVEMENT_MARGIN,
    private val thresholds: ClassifierThresholds = ClassifierThresholds(),
) {
    fun ownWeight(validSessions: Int): Double = validSessions.toDouble() / (validSessions + k)

    fun blend(category: ActivityProfile, learning: ActivityLearning?): ActivityProfile {
        if (learning == null) return category
        val own = ownWeight(learning.validSessions)
        val categoryLimit = thresholds.movementLimitFor(category.normalMovement)
        return category.copy(
            targetBlock = learning.meanDuration?.let { it * own + category.targetBlock * (1 - own) }
                ?: category.targetBlock,
            movementLimit = learning.meanMovement?.let { it * movementMargin * own + categoryLimit * (1 - own) },
            sensitivityAdjustment = learning.sensitivityAdjustment,
        )
    }

    companion object {
        const val DEFAULT_K = 5
        const val DEFAULT_MOVEMENT_MARGIN = 1.5
    }
}
