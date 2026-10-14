package com.stateai.domain.pause

import com.stateai.domain.session.Suggestion
import com.stateai.domain.session.SuggestionOutcome
import com.stateai.domain.session.SuggestionReason
import com.stateai.domain.testing.FakePauseResponseRepository
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PauseAdvisorTest {
    private val repository = FakePauseResponseRepository()
    private val restless = SuggestionReason.RESTLESS

    @Test
    fun `everything is suggested until there is enough history`() = runTest {
        val advisor = advisor()

        repeat(5) { advisor.record(suggestion(restless), SuggestionOutcome.DISMISSED) }

        assertTrue(advisor.shouldSuggest(restless))
    }

    @Test
    fun `a kind that is almost never taken is held back`() = runTest {
        val advisor = advisor()

        repeat(6) { advisor.record(suggestion(restless), SuggestionOutcome.IGNORED) }

        assertFalse(advisor.shouldSuggest(restless))
    }

    @Test
    fun `a kind that is taken often keeps being suggested`() = runTest {
        val advisor = advisor()

        repeat(3) { advisor.record(suggestion(restless), SuggestionOutcome.ACCEPTED) }
        repeat(7) { advisor.record(suggestion(restless), SuggestionOutcome.DISMISSED) }

        assertTrue(advisor.shouldSuggest(restless))
    }

    @Test
    fun `holding back one kind leaves the others alone`() = runTest {
        val advisor = advisor()

        repeat(6) { advisor.record(suggestion(restless), SuggestionOutcome.DISMISSED) }

        assertTrue(advisor.shouldSuggest(SuggestionReason.LEFT_FOCUS))
    }

    @Test
    fun `the overload alert is never held back`() = runTest {
        val advisor = advisor()

        repeat(20) { advisor.record(suggestion(SuggestionReason.OVERLOAD), SuggestionOutcome.IGNORED) }

        assertTrue(advisor.shouldSuggest(SuggestionReason.OVERLOAD))
    }

    @Test
    fun `a held back kind is tried again every fifth time so the person can change their mind`() = runTest {
        val advisor = advisor()
        repeat(6) { advisor.record(suggestion(restless), SuggestionOutcome.DISMISSED) }

        val offered = List(10) { advisor.shouldSuggest(restless) }

        assertEquals(listOf(false, false, false, false, true), offered.take(5))
        assertEquals(2, offered.count { it })
    }

    @Test
    fun `accepting the retries makes the kind welcome again`() = runTest {
        val advisor = advisor()
        repeat(6) { advisor.record(suggestion(restless), SuggestionOutcome.DISMISSED) }

        repeat(3) { advisor.record(suggestion(restless), SuggestionOutcome.ACCEPTED) }

        assertTrue(advisor.shouldSuggest(restless))
        assertTrue(advisor.shouldSuggest(restless))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `responses are saved and survive a restart`() = runTest {
        val advisor = advisor()
        repeat(4) { advisor.record(suggestion(restless), SuggestionOutcome.DISMISSED) }
        advisor.record(suggestion(restless), SuggestionOutcome.ACCEPTED)
        advanceUntilIdle()

        assertEquals(PauseResponses(shown = 5, accepted = 1), repository.stored[restless])

        val restarted = advisor()
        restarted.load()
        assertEquals(PauseResponses(shown = 5, accepted = 1), restarted.responsesFor(restless))
    }

    private fun TestScope.advisor() = PauseAdvisor(repository, this)

    private fun suggestion(reason: SuggestionReason) = Suggestion(reason, Instant.EPOCH, 10.minutes)
}
