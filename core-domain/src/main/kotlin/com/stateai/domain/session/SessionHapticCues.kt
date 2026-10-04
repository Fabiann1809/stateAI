package com.stateai.domain.session

import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.haptics.HapticPlayer
import java.time.Instant

/**
 * Time-based haptic cues of a session: one short pulse when it starts and one suggested pause
 * when the target block is reached. Each cue fires at most once per session and is reported to
 * [onCue] so cycle detection can discount the periodicity the app itself induces.
 */
class SessionHapticCues(private val player: HapticPlayer, private val onCue: (Instant) -> Unit = {}) {
    private var startedSession: ActiveSession? = null
    private var completedSession: ActiveSession? = null

    fun onTick(session: ActiveSession, now: Instant) {
        if (startedSession != session) {
            startedSession = session
            player.play(HapticEvent.BLOCK_START)
            onCue(now)
        }
        if (completedSession != session && session.progressAt(now).isTargetReached) {
            completedSession = session
            player.play(HapticEvent.PAUSE_SUGGESTED)
            onCue(now)
        }
    }
}
