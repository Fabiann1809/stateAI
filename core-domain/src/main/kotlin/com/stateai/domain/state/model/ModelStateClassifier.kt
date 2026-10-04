package com.stateai.domain.state.model

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.ActivityProfile
import com.stateai.domain.state.RuleBasedClassifier
import com.stateai.domain.state.StateClassifier
import com.stateai.domain.state.StateEstimate
import kotlin.time.Duration

/**
 * Activation level from the learned model. The model does not predict restlessness, so that flag
 * still comes from the rules. When the model fails or is not confident enough, the rules decide.
 */
class ModelStateClassifier(
    private val model: LevelModel,
    private val rules: RuleBasedClassifier = RuleBasedClassifier(),
    private val minConfidence: Float = DEFAULT_MIN_CONFIDENCE,
) : StateClassifier {
    override fun classify(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: ActivityProfile,
        elapsedInSession: Duration,
    ): StateEstimate? {
        val ruleEstimate = rules.classify(window, baseline, profile, elapsedInSession)
        val level = confidentLevel(window, baseline, elapsedInSession) ?: return ruleEstimate
        return StateEstimate(level, ruleEstimate?.restless ?: false)
    }

    /** The model's level, or null when there is no heart rate or the model fails or is unsure. */
    private fun confidentLevel(window: FeatureWindow, baseline: UserBaseline, elapsedInSession: Duration) =
        ModelInputs.of(window, baseline, elapsedInSession)
            ?.let { inputs -> runCatching { LevelPrediction.from(model.probabilities(inputs)) }.getOrNull() }
            ?.takeIf { it.confidence >= minConfidence }
            ?.level

    companion object {
        /** Below this top-class probability the model is treated as unsure (3 levels: chance is 1/3). */
        const val DEFAULT_MIN_CONFIDENCE = 0.5f
    }
}
