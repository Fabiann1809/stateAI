package com.stateai.di

import android.content.Context
import com.stateai.domain.haptics.HapticPlayer
import com.stateai.domain.haptics.HapticRateLimiter
import com.stateai.domain.haptics.RateLimitedHapticPlayer
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.session.SessionTracker
import com.stateai.haptics.VibratorHapticPlayer
import java.time.Clock

/** Haptic players: a raw one and one limited by the running session's profile. */
class HapticsModule(context: Context, clock: Clock, sessionTracker: SessionTracker) {
    /** Plays every event; used by the debug screen and outside sessions. */
    val player: HapticPlayer = VibratorHapticPlayer(context)

    /** Plays events within the rate limits of the running session's profile. */
    val sessionPlayer: HapticPlayer = RateLimitedHapticPlayer(
        delegate = player,
        limiter = HapticRateLimiter(clock),
        maxPerHour = {
            sessionTracker.activeSession.value?.profile?.maxVibrationsPerHour
                ?: DefaultCategoryProfiles.OTHER.maxVibrationsPerHour
        },
    )
}
