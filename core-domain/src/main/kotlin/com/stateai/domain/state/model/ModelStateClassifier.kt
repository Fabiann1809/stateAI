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
 * still comes from the rules.
 */
class ModelStateClassifier(
    private val model: LevelModel,
    private val rules: RuleBasedClassifier = RuleBasedClassifier(),
) : StateClassifier {
    override fun classify(
        window: FeatureWindow,
        baseline: UserBaseline,
        profile: ActivityProfile,
        elapsedInSession: Duration,
    ): StateEstimate? {
        val inputs = ModelInputs.of(window, baseline, elapsedInSession) ?: return null
        val prediction = LevelPrediction.from(model.probabilities(inputs))
        val restless = rules.classify(window, baseline, profile, elapsedInSession)?.restless ?: false
        return StateEstimate(prediction.level, restless)
    }
}
