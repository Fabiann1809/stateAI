package com.stateai.domain.learning

import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.haptics.HapticPlayer
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Tells whether now is a learned focus window and plays the "focus window" pulse (SPEC 6.2)
 * at most once per hour.
 */
class FocusWindowNotifier(
    private val observeFocusProfile: ObserveFocusProfile,
    private val player: HapticPlayer,
    private val clock: Clock,
    private val advisor: FocusWindowAdvisor = FocusWindowAdvisor(),
) {
    private var lastNotifiedHour: LocalDateTime? = null

    fun observe(): Flow<Boolean> = observeFocusProfile().map { profile ->
        val now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.HOURS)
        advisor.isFocusWindow(profile, now.hour).also { isWindow ->
            if (isWindow && lastNotifiedHour != now) {
                lastNotifiedHour = now
                player.play(HapticEvent.BLOCK_START)
            }
        }
    }
}
