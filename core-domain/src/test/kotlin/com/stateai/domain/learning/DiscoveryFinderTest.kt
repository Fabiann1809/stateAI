package com.stateai.domain.learning

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.Segment
import com.stateai.domain.testing.testSegment
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class DiscoveryFinderTest {
    private val finder = DiscoveryFinder(ZoneOffset.UTC)

    @Test
    fun `day parts follow the local hour`() {
        assertEquals(DayPart.EVENING, DayPart.of(4))
        assertEquals(DayPart.MORNING, DayPart.of(5))
        assertEquals(DayPart.MORNING, DayPart.of(11))
        assertEquals(DayPart.AFTERNOON, DayPart.of(12))
        assertEquals(DayPart.AFTERNOON, DayPart.of(17))
        assertEquals(DayPart.EVENING, DayPart.of(18))
        assertEquals(DayPart.EVENING, DayPart.of(23))
    }

    @Test
    fun `without ratings there is nothing to say`() {
        val unrated = List(10) { session(day = it + 1, hour = 9, feedback = null) }

        assertEquals(Discovery.NotEnoughData(0), finder.find(unrated))
    }

    @Test
    fun `a few rated sessions are not enough`() {
        val few = listOf(session(1, 9, Feedback.GOOD), session(2, 9, Feedback.GOOD))

        assertEquals(Discovery.NotEnoughData(2), finder.find(few))
    }

    @Test
    fun `finds the moment of the day that is rated higher`() {
        val sessions = morning(Feedback.VERY_GOOD, Feedback.GOOD, Feedback.GOOD) +
            afternoon(Feedback.OKAY, Feedback.BAD, Feedback.OKAY)

        val found = assertInstanceOf(Discovery.BetterMoment::class.java, finder.find(sessions))

        assertEquals(DayPart.MORNING, found.part)
        assertEquals(ActivityCategory.STUDY, found.category)
        assertEquals(13.0 / 3, found.average, 1e-9)
        assertEquals(8.0 / 3, found.otherAverage, 1e-9)
        assertEquals(3, found.sessions)
        assertEquals(3, found.otherSessions)
        assertEquals(DiscoveryConfidence.PROVISIONAL, found.confidence)
    }

    @Test
    fun `a small difference is not a finding`() {
        val sessions = morning(Feedback.GOOD, Feedback.GOOD, Feedback.OKAY) +
            afternoon(Feedback.GOOD, Feedback.OKAY, Feedback.OKAY)

        assertEquals(Discovery.NoClearDifference(6), finder.find(sessions))
    }

    @Test
    fun `a moment seen on a single day is not enough, however many sessions`() {
        val sameDay = List(4) { session(day = 1, hour = 9 + it, feedback = Feedback.VERY_GOOD) }
        val others = afternoon(Feedback.BAD, Feedback.BAD, Feedback.BAD)

        assertEquals(Discovery.NotEnoughData(7), finder.find(sameDay + others))
    }

    @Test
    fun `many sessions and a big difference give a moderate confidence`() {
        val sessions = List(6) { session(it + 1, 9, Feedback.VERY_GOOD) } +
            List(6) { session(it + 1, 15, Feedback.OKAY) }

        val found = assertInstanceOf(Discovery.BetterMoment::class.java, finder.find(sessions))

        assertEquals(DiscoveryConfidence.MODERATE, found.confidence)
    }

    @Test
    fun `different kinds of activity are never compared with each other`() {
        val study = morning(Feedback.VERY_GOOD, Feedback.VERY_GOOD, Feedback.VERY_GOOD)
        val meetings = afternoon(Feedback.BAD, Feedback.BAD, Feedback.BAD)
            .map { it.copy(activity = Activity(ActivityId("meeting"), ActivityCategory.COLLAB, name = null)) }

        assertEquals(Discovery.NotEnoughData(6), finder.find(study + meetings))
    }

    @Test
    fun `uses the local time zone to place a session in the day`() {
        val local = DiscoveryFinder(ZoneOffset.ofHours(-5))
        val sessions = List(3) { session(it + 1, 14, Feedback.VERY_GOOD) } +
            List(3) { session(it + 1, 23, Feedback.BAD) }

        val found = assertInstanceOf(Discovery.BetterMoment::class.java, local.find(sessions))

        assertEquals(DayPart.MORNING, found.part)
    }

    private fun morning(vararg ratings: Feedback) = ratings.mapIndexed { i, rating -> session(i + 1, 9, rating) }

    private fun afternoon(vararg ratings: Feedback) = ratings.mapIndexed { i, rating -> session(i + 1, 15, rating) }

    private fun session(day: Int, hour: Int, feedback: Feedback?): Segment = testSegment(
        id = "s-$day-$hour",
        start = Instant.parse("2026-10-%02dT%02d:00:00Z".format(day, hour)),
        feedback = feedback,
    )
}
