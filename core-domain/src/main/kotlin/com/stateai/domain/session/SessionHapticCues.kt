package com.stateai.domain.session

import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.haptics.HapticPlayer
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Time-based haptic cues of a session: one short pulse when it starts, one suggested pause when the
 * target block is reached, and a gentle reminder every [reminderEvery] while the person keeps going
 * past it. Every cue is reported to [onCue] so cycle detection can discount the periodicity the app
 * itself induces, and pauses are posted to [suggestions] so the screen shows them. The vibrations
 * still go through the player's rate limits.
 */
class SessionHapticCues(
    private val player: HapticPlayer,
    private val onCue: (Instant) -> Unit = {},
    private val suggestions: SessionSuggestions = SessionSuggestions(),
    private val reminderEvery: Duration = DEFAULT_REMINDER_EVERY,
) {
    private var startedSession: ActiveSession? = null
    private var nextPauseAt: Duration? = null

    fun onTick(session: ActiveSession, now: Instant) {
        if (startedSession != session) {
            startedSession = session
            nextPauseAt = session.profile.targetBlock
            player.play(HapticEvent.BLOCK_START)
            onCue(now)
        }
        val progress = session.progressAt(now)
        val due = nextPauseAt ?: return
        if (progress.elapsed >= due) {
            val blockDone = due == session.profile.targetBlock
            val reason = if (blockDone) SuggestionReason.BLOCK_DONE else SuggestionReason.LONG_SESSION
            nextPauseAt = due + reminderEvery
            player.play(HapticEvent.PAUSE_SUGGESTED)
            onCue(now)
            suggestions.post(Suggestion(reason, now, progress.elapsed))
        }
    }

    private companion object {
        val DEFAULT_REMINDER_EVERY = 20.minutes
    }
}
