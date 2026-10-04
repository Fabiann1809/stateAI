package com.stateai.domain.score

/**
 * Focus-and-load score from 0 to 100, always shown with its components (SPEC 6.6).
 * It measures quality of focus and load, not work produced.
 */
data class ScoreBreakdown(
    val focus: Double,
    val recovery: Double,
    val sustainableLoad: Double,
    val consistency: Double,
) {
    fun total(weights: ScoreWeights = ScoreWeights()): Double = focus * weights.focus +
        recovery * weights.recovery +
        sustainableLoad * weights.sustainableLoad +
        consistency * weights.consistency
}

/** Component weights; they add up to 1. */
data class ScoreWeights(
    val focus: Double = 0.40,
    val recovery: Double = 0.25,
    val sustainableLoad: Double = 0.20,
    val consistency: Double = 0.15,
)
