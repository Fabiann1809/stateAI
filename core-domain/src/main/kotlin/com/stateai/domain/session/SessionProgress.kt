package com.stateai.domain.session

import kotlin.time.Duration

/** Elapsed time of a session compared with its target block. */
data class SessionProgress(val elapsed: Duration, val target: Duration) {
    val isTargetReached: Boolean get() = elapsed >= target

    /** Share of the target completed, from 0 to 1. */
    val fraction: Float
        get() = if (target <= Duration.ZERO) 1f else (elapsed / target).toFloat().coerceIn(0f, 1f)
}
