package com.stateai.domain.state

/**
 * The state shown to the person. "Exhausted" is not a fourth classifier level: it is derived from
 * sustained overload that comes with restlessness or with low estimated energy (decision 26).
 */
enum class DisplayState {
    FOCUSED,
    NORMAL,
    OVERLOADED,
    EXHAUSTED,
    ;

    companion object {
        fun of(estimate: StateEstimate, energyIsLow: Boolean): DisplayState = when (estimate.level) {
            ActivationLevel.LOW -> FOCUSED
            ActivationLevel.MEDIUM -> NORMAL
            ActivationLevel.HIGH -> if (estimate.restless || energyIsLow) EXHAUSTED else OVERLOADED
        }
    }
}
