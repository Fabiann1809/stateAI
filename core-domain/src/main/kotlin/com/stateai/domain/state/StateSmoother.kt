package com.stateai.domain.state

/**
 * Hysteresis: the first estimate is accepted as is; afterwards the level and the restlessness flag
 * only change when the same new value is seen in [requiredRepeats] consecutive windows.
 */
class StateSmoother(private val requiredRepeats: Int = DEFAULT_REQUIRED_REPEATS) {
    private val level = Debounced<ActivationLevel>(requiredRepeats)
    private val restless = Debounced<Boolean>(requiredRepeats)

    fun update(raw: StateEstimate): StateEstimate =
        StateEstimate(level = level.update(raw.level), restless = restless.update(raw.restless))

    private class Debounced<T : Any>(private val requiredRepeats: Int) {
        private var stable: T? = null
        private var candidate: T? = null
        private var candidateCount = 0

        fun update(value: T): T {
            val current = stable
            when {
                current == null || value == current -> resetCandidate().also { stable = value }
                value == candidate -> candidateCount++
                else -> {
                    candidate = value
                    candidateCount = 1
                }
            }
            if (candidateCount >= requiredRepeats) {
                stable = candidate
                resetCandidate()
            }
            return requireNotNull(stable)
        }

        private fun resetCandidate() {
            candidate = null
            candidateCount = 0
        }
    }

    companion object {
        const val DEFAULT_REQUIRED_REPEATS = 2
    }
}
