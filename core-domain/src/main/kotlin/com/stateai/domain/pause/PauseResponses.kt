package com.stateai.domain.pause

import com.stateai.domain.session.SuggestionReason

/**
 * What the person has done with one kind of pause suggestion.
 *
 * @property shown how many times it was offered and answered or left unanswered.
 * @property accepted how many of those the person took.
 * @property skipped how many times it was held back since it stopped being welcome.
 */
data class PauseResponses(val shown: Int = 0, val accepted: Int = 0, val skipped: Int = 0) {
    val acceptRate: Double get() = if (shown == 0) 0.0 else accepted.toDouble() / shown
}

/** Storage of the responses to pause suggestions, one entry per [SuggestionReason]. */
interface PauseResponseRepository {
    suspend fun load(): Map<SuggestionReason, PauseResponses>

    suspend fun save(reason: SuggestionReason, responses: PauseResponses)
}
