package com.stateai.domain.session

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.testing.MutableClock
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SessionHapticCuesTest {
    private val clock = MutableClock()
    private val played = mutableListOf<HapticEvent>()
    private val suggestions = SessionSuggestions()
    private val cues =
        SessionHapticCues(player = { played += it }, onCue = { cueTimes += it }, suggestions = suggestions)
    private val cueTimes = mutableListOf<Instant>()
    private val study = Activity(ActivityId("study"), ActivityCategory.STUDY, name = null)
    private val session = ActiveSession(study, DefaultCategoryProfiles.STUDY, startedAt = clock.instant())

    @Test
    fun `plays a short pulse once when the session starts`() {
        cues.onTick(session, clock.instant())
        clock.advanceBy(1.minutes)
        cues.onTick(session, clock.instant())

        assertEquals(listOf(HapticEvent.BLOCK_START), played)
    }

    @Test
    fun `suggests a pause once when the target block is reached`() {
        cues.onTick(session, clock.instant())
        clock.advanceBy(40.minutes)
        cues.onTick(session, clock.instant())
        clock.advanceBy(5.minutes)
        cues.onTick(session, clock.instant())

        assertEquals(listOf(HapticEvent.BLOCK_START, HapticEvent.PAUSE_SUGGESTED), played)
        assertEquals(2, cueTimes.size)
    }

    @Test
    fun `does not suggest a pause before the target`() {
        cues.onTick(session, clock.instant())
        clock.advanceBy(39.minutes)
        cues.onTick(session, clock.instant())

        assertEquals(listOf(HapticEvent.BLOCK_START), played)
    }

    @Test
    fun `a new session gets its own cues`() {
        cues.onTick(session, clock.instant())
        val next = session.copy(startedAt = clock.instant().plusSeconds(60))
        cues.onTick(next, clock.instant())

        assertEquals(listOf(HapticEvent.BLOCK_START, HapticEvent.BLOCK_START), played)
    }

    @Test
    fun `reaching the block posts a rest suggestion`() {
        cues.onTick(session, clock.instant())
        clock.advanceBy(40.minutes)
        cues.onTick(session, clock.instant())

        assertEquals(SuggestionReason.BLOCK_DONE, suggestions.current.value?.reason)
        assertEquals(PauseKind.REST, suggestions.current.value?.reason?.pause)
        assertEquals(40.minutes, suggestions.current.value?.elapsed)
    }

    @Test
    fun `keeps reminding every twenty minutes past the block`() {
        cues.onTick(session, clock.instant())
        repeat(80) {
            clock.advanceBy(1.minutes)
            cues.onTick(session, clock.instant())
        }

        assertEquals(3, played.count { it == HapticEvent.PAUSE_SUGGESTED })
        assertEquals(SuggestionReason.LONG_SESSION, suggestions.current.value?.reason)
        assertEquals(80.minutes, suggestions.current.value?.elapsed)
    }
}
