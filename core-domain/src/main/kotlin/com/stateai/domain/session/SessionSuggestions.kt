package com.stateai.domain.session

import java.time.Instant
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** What kind of pause a suggestion offers. */
enum class PauseKind {
    /** The guided breathing (about a minute). */
    BREATHE,

    /** A short break away from the task: stand up, stretch, drink water. */
    REST,
}

/** Why the watch suggests a pause; each reason maps to the pause that fits it best. */
enum class SuggestionReason(val pause: PauseKind) {
    /** Overload sustained for a few minutes. */
    OVERLOAD(PauseKind.BREATHE),

    /** Sustained restlessness: better to move a little. */
    RESTLESS(PauseKind.REST),

    /** A long focus stretch just ended: a breath before going on. */
    LEFT_FOCUS(PauseKind.BREATHE),

    /** The planned block is done. */
    BLOCK_DONE(PauseKind.REST),

    /** Still working well past the planned block. */
    LONG_SESSION(PauseKind.REST),
}

/** A pause suggestion shown during the session; [elapsed] is the session time when it was made. */
data class Suggestion(val reason: SuggestionReason, val at: Instant, val elapsed: Duration)

/**
 * The latest pause suggestion of the running session, so the screen can show it next to the
 * vibration. It only suggests: dismissing it or ignoring it changes nothing (SPEC 2, "recommend, do
 * not impose"). A newer suggestion replaces the previous one.
 */
class SessionSuggestions {
    private val latest = MutableStateFlow<Suggestion?>(null)
    val current: StateFlow<Suggestion?> = latest.asStateFlow()

    fun post(suggestion: Suggestion) {
        latest.value = suggestion
    }

    fun dismiss() {
        latest.value = null
    }
}
