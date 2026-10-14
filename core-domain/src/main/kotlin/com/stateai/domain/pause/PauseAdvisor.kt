package com.stateai.domain.pause

import com.stateai.domain.session.Suggestion
import com.stateai.domain.session.SuggestionGate
import com.stateai.domain.session.SuggestionOutcome
import com.stateai.domain.session.SuggestionReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * When a kind of suggestion stops being welcome and how often it is tried again.
 *
 * @property minShown suggestions of a kind needed before judging whether it is welcome.
 * @property minAcceptRate below this share of accepted suggestions, a kind is not welcome.
 * @property retryEvery a kind that is not welcome is still offered once every this many times, so
 *   the person can change their mind.
 */
data class PauseAdvisorConfig(val minShown: Int = 6, val minAcceptRate: Double = 0.2, val retryEvery: Int = 5)

/**
 * Learns which pause suggestions the person takes and holds back the ones they keep turning down.
 * It only makes the watch quieter: it never invents new suggestions, and it never holds back the
 * overload alert, which is about load and not about taking a break. Responses are kept in memory
 * after [load] and saved in the background.
 */
class PauseAdvisor(
    private val repository: PauseResponseRepository,
    private val scope: CoroutineScope,
    private val config: PauseAdvisorConfig = PauseAdvisorConfig(),
) : SuggestionGate {
    private val lock = Any()
    private val saveLock = Mutex()
    private var responses: Map<SuggestionReason, PauseResponses> = emptyMap()

    suspend fun load() {
        val stored = repository.load()
        synchronized(lock) { responses = stored + responses }
    }

    override fun shouldSuggest(reason: SuggestionReason): Boolean {
        val skipped = if (reason == SuggestionReason.OVERLOAD) null else skipOne(reason)
        return skipped == null || skipped % config.retryEvery == 0
    }

    /** Counts one more held-back suggestion of [reason], or null when it is still welcome. */
    private fun skipOne(reason: SuggestionReason): Int? {
        val skipped = synchronized(lock) {
            val current = responses[reason] ?: PauseResponses()
            if (isWelcome(current)) null else update(reason, current.copy(skipped = current.skipped + 1)).skipped
        }
        if (skipped != null) persist(reason)
        return skipped
    }

    fun record(suggestion: Suggestion, outcome: SuggestionOutcome) {
        val reason = suggestion.reason
        synchronized(lock) {
            val current = responses[reason] ?: PauseResponses()
            update(
                reason,
                current.copy(
                    shown = current.shown + 1,
                    accepted = current.accepted + if (outcome == SuggestionOutcome.ACCEPTED) 1 else 0,
                ),
            )
        }
        persist(reason)
    }

    fun responsesFor(reason: SuggestionReason): PauseResponses =
        synchronized(lock) { responses[reason] ?: PauseResponses() }

    private fun isWelcome(current: PauseResponses) =
        current.shown < config.minShown || current.acceptRate >= config.minAcceptRate

    private fun update(reason: SuggestionReason, next: PauseResponses): PauseResponses {
        responses = responses + (reason to next)
        return next
    }

    /** Saves the latest responses, so a late save never overwrites a newer one. */
    private fun persist(reason: SuggestionReason) {
        scope.launch { saveLock.withLock { repository.save(reason, responsesFor(reason)) } }
    }
}
