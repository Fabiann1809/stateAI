package com.stateai.domain.learning

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.testing.testSegment
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ActivityPredictorTest {
    private val predictor = ActivityPredictor(ZoneOffset.UTC)
    private val study = Activity(ActivityId("study"), ActivityCategory.STUDY, name = null)
    private val reading = Activity(ActivityId("reading"), ActivityCategory.READING, name = null)

    // 2026-10-05 is a Monday.
    private val mondayAtNine = ZonedDateTime.parse("2026-10-12T09:15:00Z")

    @Test
    fun `suggests the activity usually started at this hour`() {
        val history = (5..9).map { day -> segment(study, "2026-10-0${day}T09:05:00Z") } +
            (5..9).map { day -> segment(reading, "2026-10-0${day}T20:00:00Z") }

        assertEquals(study.id, predictor.predict(history, mondayAtNine))
    }

    @Test
    fun `the same weekday counts more than other days`() {
        val history = listOf(
            segment(reading, "2026-10-05T09:00:00Z"),
            segment(reading, "2026-09-28T09:00:00Z"),
            segment(study, "2026-10-06T09:00:00Z"),
            segment(study, "2026-10-07T09:00:00Z"),
            segment(study, "2026-10-08T09:00:00Z"),
        )

        assertEquals(reading.id, predictor.predict(history, mondayAtNine))
    }

    @Test
    fun `no suggestion without enough history`() {
        val history = listOf(segment(study, "2026-10-06T09:00:00Z"), segment(study, "2026-10-07T09:00:00Z"))

        assertNull(predictor.predict(history, mondayAtNine))
    }

    private fun segment(activity: Activity, start: String) =
        testSegment(id = "${activity.id.value}-$start", activity = activity, start = Instant.parse(start))
}
