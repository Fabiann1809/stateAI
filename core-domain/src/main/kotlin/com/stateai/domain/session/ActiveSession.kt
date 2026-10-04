package com.stateai.domain.session

import com.stateai.domain.activity.Activity
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/** A running session. Elapsed time is derived from [startedAt], so it survives UI recreation. */
data class ActiveSession(val activity: Activity, val targetBlock: Duration, val startedAt: Instant) {
    fun progressAt(now: Instant): SessionProgress {
        val elapsed = java.time.Duration.between(startedAt, now).toKotlinDuration()
        return SessionProgress(elapsed = elapsed.coerceAtLeast(Duration.ZERO), target = targetBlock)
    }
}
