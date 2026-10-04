package com.stateai.domain.export

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SegmentCsvTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `writes a header and one row per segment`() {
        val lines = SegmentCsv().write(listOf(segment("Tesis \"cap 2\""))).trim().lines()

        assertEquals(SegmentCsv.HEADER.joinToString(","), lines[0])
        assertEquals(
            "s1,STUDY,\"Tesis \"\"cap 2\"\"\",2026-10-04T09:00:00Z,2026-10-04T09:40:00Z,2400," +
                "2400,0,0,0,0,0,0,0,GOOD,100.0,100.0,100.0,100.0,100.0",
            lines[1],
        )
    }

    @Test
    fun `segments are written in chronological order`() {
        val later = segment("b").copy(id = SegmentId("later"), start = start.plusSeconds(3_600))
        val earlier = segment("a").copy(id = SegmentId("earlier"))

        val ids = SegmentCsv().write(listOf(later, earlier)).trim().lines().drop(1).map { it.substringBefore(",") }

        assertEquals(listOf("earlier", "later"), ids)
    }

    private fun segment(name: String) = Segment(
        id = SegmentId("s1"),
        activity = Activity(ActivityId("a"), ActivityCategory.STUDY, ActivityName.of(name)),
        start = start,
        end = start.plusSeconds(2_400),
        planned = 40.minutes,
        levelTime = LevelDurations(low = 40.minutes),
        restlessTime = Duration.ZERO,
        pauseSuggestions = 0,
        pauses = emptyList(),
        feedback = Feedback.GOOD,
    )
}
