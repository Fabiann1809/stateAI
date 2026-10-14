package com.stateai.domain.session

import java.time.Instant
import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

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

/** What the person did with a suggestion. */
enum class SuggestionOutcome {
    /** They took the pause that was offered. */
    ACCEPTED,

    /** They answered "Ahora no". */
    DISMISSED,

    /** It was replaced by a newer one or the session ended before they answered. */
    IGNORED,
}

/** Decides whether a kind of suggestion is worth showing to this person. */
fun interface SuggestionGate {
    fun shouldSuggest(reason: SuggestionReason): Boolean

    companion object {
        val Always = SuggestionGate { true }
    }
}

/**
 * The latest pause suggestion of the running session, so the screen can show it next to the
 * vibration. It only suggests: dismissing it or ignoring it changes nothing (SPEC 2, "recommend, do
 * not impose"). A newer suggestion replaces the previous one. What the person did with each
 * suggestion is reported to [onOutcome] exactly once.
 */
class SessionSuggestions(private val onOutcome: (Suggestion, SuggestionOutcome) -> Unit = { _, _ -> }) {
    private val latest = MutableStateFlow<Suggestion?>(null)
    val current: StateFlow<Suggestion?> = latest.asStateFlow()

    fun post(suggestion: Suggestion) {
        close(SuggestionOutcome.IGNORED)
        latest.value = suggestion
    }

    /** The person took the pause. */
    fun accept() = close(SuggestionOutcome.ACCEPTED)

    /** The person answered "Ahora no". */
    fun dismiss() = close(SuggestionOutcome.DISMISSED)

    /** Clears the suggestion without an answer, at the start or the end of a session. */
    fun reset() = close(SuggestionOutcome.IGNORED)

    private fun close(outcome: SuggestionOutcome) {
        val previous = latest.getAndUpdate { null } ?: return
        onOutcome(previous, outcome)
    }
}
