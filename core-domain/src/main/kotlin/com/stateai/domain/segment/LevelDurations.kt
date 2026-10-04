package com.stateai.domain.segment

import com.stateai.domain.state.ActivationLevel
import kotlin.time.Duration

/** Time spent at each activation level; `unknown` covers calibration and time without estimates. */
data class LevelDurations(
    val low: Duration = Duration.ZERO,
    val medium: Duration = Duration.ZERO,
    val high: Duration = Duration.ZERO,
    val unknown: Duration = Duration.ZERO,
) {
    val known: Duration get() = low + medium + high
    val total: Duration get() = known + unknown

    fun plus(level: ActivationLevel?, duration: Duration): LevelDurations = when (level) {
        ActivationLevel.LOW -> copy(low = low + duration)
        ActivationLevel.MEDIUM -> copy(medium = medium + duration)
        ActivationLevel.HIGH -> copy(high = high + duration)
        null -> copy(unknown = unknown + duration)
    }
}
