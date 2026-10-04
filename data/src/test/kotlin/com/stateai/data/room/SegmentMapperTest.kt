package com.stateai.data.room

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SegmentMapperTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `segment survives the round trip through entities`() {
        val segment = Segment(
            id = SegmentId("s1"),
            activity = Activity(ActivityId("a1"), ActivityCategory.STUDY, ActivityName.of("Bases de datos")),
            start = start,
            end = start.plusSeconds(1_800),
            planned = 40.minutes,
            levelTime = LevelDurations(low = 20.minutes, medium = 5.minutes, high = 3.minutes, unknown = 2.minutes),
            restlessTime = 4.minutes,
            pauseSuggestions = 1,
            pauses = listOf(
                PauseRecord(
                    start.plusSeconds(600),
                    start.plusSeconds(720),
                    StateEstimate(ActivationLevel.HIGH, false),
                    StateEstimate(ActivationLevel.LOW, false),
                ),
            ),
            feedback = Feedback.OKAY,
        )

        val row = SegmentWithPauses(segment.toEntity(), segment.pauses.map { it.toEntity("s1") })

        assertEquals(segment, row.toDomain())
        assertEquals("Bases de datos", row.toDomain().activity.name?.display)
    }

    @Test
    fun `activity survives the round trip through its entity`() {
        val activity = Activity(ActivityId("a1"), ActivityCategory.READING, ActivityName.of("Novela"))

        assertEquals(activity, activity.toEntity(createdAtMillis = 0).toDomain())
    }
}
