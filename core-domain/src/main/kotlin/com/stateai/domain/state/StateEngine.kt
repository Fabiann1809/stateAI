package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureExtractor
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.CategoryProfile
import kotlin.time.Duration

/**
 * Turns feature windows into the session's current estimate. Windows that are not clean
 * (sustained movement or missing heart rate) carry no evidence, so the previous estimate is kept.
 */
class StateEngine(
    private val classifier: RuleBasedClassifier = RuleBasedClassifier(),
    private val extractor: FeatureExtractor = FeatureExtractor(),
) {
    var current: StateEstimate? = null
        private set

    fun onWindow(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: CategoryProfile,
        elapsedInSession: Duration,
    ): StateEstimate? {
        if (!extractor.isClean(window)) return current
        classifier.classify(window, baseline, profile, elapsedInSession)?.let { current = it }
        return current
    }
}
