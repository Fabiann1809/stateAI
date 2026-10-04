package com.stateai.domain.session

import com.stateai.domain.activity.Activity
import com.stateai.domain.profile.ActivityProfile
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/** A running session. Elapsed time is derived from [startedAt], so it survives UI recreation. */
data class ActiveSession(val activity: Activity, val profile: ActivityProfile, val startedAt: Instant) {
    val targetBlock: Duration get() = profile.targetBlock

    fun progressAt(now: Instant): SessionProgress {
        val elapsed = java.time.Duration.between(startedAt, now).toKotlinDuration()
        return SessionProgress(elapsed = elapsed.coerceAtLeast(Duration.ZERO), target = targetBlock)
    }
}
