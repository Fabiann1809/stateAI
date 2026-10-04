package com.stateai.di

import android.content.Context
import com.stateai.BuildConfig
import com.stateai.domain.state.RuleBasedClassifier
import com.stateai.domain.state.StateClassifier
import com.stateai.domain.state.model.ModelStateClassifier
import com.stateai.ml.TfLiteLevelModel

/** The state classifier chosen at build time (`-Pstateai.classifier`), rules by default. */
class ClassifierModule(context: Context) {
    val classifier: StateClassifier = if (BuildConfig.USE_MODEL_CLASSIFIER) {
        ModelStateClassifier(TfLiteLevelModel(context))
    } else {
        RuleBasedClassifier()
    }
}
