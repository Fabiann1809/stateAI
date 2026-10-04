package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.CategoryProfile
import kotlin.time.Duration

/** Classifies a window with thresholds relative to the personal baseline (SPEC 6.4). */
class RuleBasedClassifier(private val thresholds: ClassifierThresholds = ClassifierThresholds()) {
    /** Returns null when the window has no heart rate. */
    fun classify(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: CategoryProfile,
        elapsedInSession: Duration,
    ): StateEstimate? {
        val meanHeartRate = window.meanHeartRate ?: return null
        val delta = meanHeartRate - baseline.restingHeartRate
        val factor = thresholds.factorFor(profile.sensitivity)
        val calm = window.meanMovement <= thresholds.movementLimitFor(profile.normalMovement)

        val level = when {
            delta >= thresholds.highMinHeartRateDelta * factor -> ActivationLevel.HIGH
            delta <= thresholds.lowMaxHeartRateDelta * factor && calm -> ActivationLevel.LOW
            else -> ActivationLevel.MEDIUM
        }
        val restless = window.fidgetCount >= thresholds.restlessMinFidgets &&
            elapsedInSession >= thresholds.restlessMinElapsed
        return StateEstimate(level, restless)
    }
}
