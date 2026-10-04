package com.stateai.domain.haptics

import java.time.Clock
import java.time.Instant
import kotlin.time.toJavaDuration

/**
 * Decides whether a vibration may play: at most one every [HapticLimits.minInterval] and at most
 * `maxPerHour` within [HapticLimits.capWindow]. Exempt events always play and are not counted.
 */
class HapticRateLimiter(private val clock: Clock, private val limits: HapticLimits = HapticLimits()) {
    private val played = ArrayDeque<Instant>()

    fun tryAcquire(event: HapticEvent, maxPerHour: Int): Boolean {
        if (event.isExemptFromRateLimit) return true

        val now = clock.instant()
        forgetOlderThan(now.minus(limits.capWindow.toJavaDuration()))
        val allowed = isIntervalRespected(now) && played.size < maxPerHour
        if (allowed) played.addLast(now)
        return allowed
    }

    private fun isIntervalRespected(now: Instant): Boolean {
        val last = played.lastOrNull() ?: return true
        return !now.isBefore(last.plus(limits.minInterval.toJavaDuration()))
    }

    private fun forgetOlderThan(limit: Instant) {
        while (played.firstOrNull()?.isBefore(limit) == true) played.removeFirst()
    }
}
