package com.stateai.domain.session

import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SessionSuggestionsTest {
    private val outcomes = mutableListOf<Pair<SuggestionReason, SuggestionOutcome>>()
    private val suggestions = SessionSuggestions { suggestion, outcome -> outcomes += suggestion.reason to outcome }

    @Test
    fun `taking the pause is reported as accepted and clears the suggestion`() {
        suggestions.post(suggestion(SuggestionReason.RESTLESS))

        suggestions.accept()

        assertEquals(listOf(SuggestionReason.RESTLESS to SuggestionOutcome.ACCEPTED), outcomes)
        assertNull(suggestions.current.value)
    }

    @Test
    fun `answering not now is reported as dismissed`() {
        suggestions.post(suggestion(SuggestionReason.LEFT_FOCUS))

        suggestions.dismiss()

        assertEquals(listOf(SuggestionReason.LEFT_FOCUS to SuggestionOutcome.DISMISSED), outcomes)
    }

    @Test
    fun `a newer suggestion makes the unanswered one ignored`() {
        suggestions.post(suggestion(SuggestionReason.BLOCK_DONE))
        suggestions.post(suggestion(SuggestionReason.LONG_SESSION))

        assertEquals(listOf(SuggestionReason.BLOCK_DONE to SuggestionOutcome.IGNORED), outcomes)
        assertEquals(SuggestionReason.LONG_SESSION, suggestions.current.value?.reason)
    }

    @Test
    fun `resetting an unanswered suggestion reports it as ignored`() {
        suggestions.post(suggestion(SuggestionReason.OVERLOAD))

        suggestions.reset()

        assertEquals(listOf(SuggestionReason.OVERLOAD to SuggestionOutcome.IGNORED), outcomes)
    }

    @Test
    fun `an answered suggestion is reported only once`() {
        suggestions.post(suggestion(SuggestionReason.RESTLESS))
        suggestions.accept()

        suggestions.dismiss()
        suggestions.reset()

        assertEquals(1, outcomes.size)
    }

    @Test
    fun `answering with nothing on screen reports nothing`() {
        suggestions.accept()
        suggestions.dismiss()
        suggestions.reset()

        assertEquals(emptyList<Pair<SuggestionReason, SuggestionOutcome>>(), outcomes)
    }

    private fun suggestion(reason: SuggestionReason) = Suggestion(reason, Instant.EPOCH, 10.minutes)
}
