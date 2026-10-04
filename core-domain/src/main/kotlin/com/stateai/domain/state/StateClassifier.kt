package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.ActivityProfile
import kotlin.time.Duration

/** Estimates the state of one clean window. Implementations: rules and the learned model. */
fun interface StateClassifier {
    /** Returns null when the window gives no estimate (for example, no heart rate). */
    fun classify(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: ActivityProfile,
        elapsedInSession: Duration,
    ): StateEstimate?
}
