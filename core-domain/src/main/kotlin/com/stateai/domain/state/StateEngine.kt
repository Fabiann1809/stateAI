package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureExtractor
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.CategoryProfile
import kotlin.time.Duration

/**
 * Turns feature windows into the session's current estimate: clean windows are classified and
 * smoothed; windows that are not clean (sustained movement or missing heart rate) carry no evidence,
 * so the previous estimate is kept.
 */
class StateEngine(
    private val classifier: RuleBasedClassifier = RuleBasedClassifier(),
    private val extractor: FeatureExtractor = FeatureExtractor(),
    private val smoother: StateSmoother = StateSmoother(),
) {
    var current: StateEstimate? = null
        private set

    /** Returns the current estimate after this window (unchanged when the window is not usable). */
    fun onWindow(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: CategoryProfile,
        elapsedInSession: Duration,
    ): StateEstimate? = update(window, baseline, profile, elapsedInSession) ?: current

    /** Returns the new estimate, or null when the window was not usable and nothing changed. */
    fun update(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: CategoryProfile,
        elapsedInSession: Duration,
    ): StateEstimate? = window
        .takeIf { extractor.isClean(it) }
        ?.let { classifier.classify(it, baseline, profile, elapsedInSession) }
        ?.let { raw -> smoother.update(raw).also { current = it } }
}
