package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.ActivityProfile
import kotlin.time.Duration

/** Classifies a window with thresholds relative to the personal baseline (SPEC 6.4). */
class RuleBasedClassifier(private val thresholds: ClassifierThresholds = ClassifierThresholds()) {
    /** Returns null when the window has no heart rate. */
    fun classify(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: ActivityProfile,
        elapsedInSession: Duration,
    ): StateEstimate? {
        val meanHeartRate = window.meanHeartRate ?: return null
        val delta = meanHeartRate - baseline.restingHeartRate
        val factor = thresholds.factorFor(profile.sensitivity) * (1 + profile.sensitivityAdjustment)
        val movementLimit = profile.movementLimit ?: thresholds.movementLimitFor(profile.normalMovement)
        val calm = window.meanMovement <= movementLimit

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
