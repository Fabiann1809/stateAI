package com.stateai.domain.haptics

/** Plays events through [delegate] only when [limiter] allows them under the current hourly cap. */
class RateLimitedHapticPlayer(
    private val delegate: HapticPlayer,
    private val limiter: HapticRateLimiter,
    private val maxPerHour: () -> Int,
) : HapticPlayer {
    override fun play(event: HapticEvent) {
        if (limiter.tryAcquire(event, maxPerHour())) delegate.play(event)
    }
}
