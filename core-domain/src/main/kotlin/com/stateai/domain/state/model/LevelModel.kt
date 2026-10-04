package com.stateai.domain.state.model

import com.stateai.domain.state.ActivationLevel

/** A learned model that maps [ModelInputs] to one probability per [ActivationLevel], in enum order. */
fun interface LevelModel {
    /** May throw when the model cannot run; callers fall back to the rules. */
    fun probabilities(inputs: FloatArray): FloatArray
}

/** The most likely level and how confident the model is about it. */
data class LevelPrediction(val level: ActivationLevel, val confidence: Float) {
    companion object {
        fun from(probabilities: FloatArray): LevelPrediction {
            require(probabilities.size == ActivationLevel.entries.size) { "One probability per level expected" }
            val best = probabilities.indices.maxBy { probabilities[it] }
            return LevelPrediction(ActivationLevel.entries[best], probabilities[best])
        }
    }
}
