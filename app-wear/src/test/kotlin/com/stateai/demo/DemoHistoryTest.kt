package com.stateai.demo

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DemoHistoryTest {
    private val today = LocalDate.parse("2026-10-05")
    private val activities = DemoRoutine.ALL.associateWith { demo ->
        Activity(ActivityId(demo.name ?: "meetings"), demo.category, demo.name?.let(ActivityName::of))
    }

    @Test
    fun `fourteen days of four sessions each, all before today`() {
        val segments = DemoHistory(ZoneOffset.UTC).segments(today, activities)

        assertEquals(14 * 4, segments.size)
        assertEquals(today.minusDays(14), segments.first().start.atZone(ZoneOffset.UTC).toLocalDate())
        assertTrue(segments.all { it.start.atZone(ZoneOffset.UTC).toLocalDate().isBefore(today) })
        assertEquals(segments.sortedBy { it.start }, segments)
    }

    @Test
    fun `the same seed gives the same history`() {
        assertEquals(
            DemoHistory(ZoneOffset.UTC).segments(today, activities),
            DemoHistory(ZoneOffset.UTC).segments(today, activities),
        )
    }

    @Test
    fun `mornings have more focus than afternoons`() {
        val segments = DemoHistory(ZoneOffset.UTC).segments(today, activities)
        fun focusShare(morning: Boolean) = segments
            .filter { (it.start.atZone(ZoneOffset.UTC).hour < 12) == morning }
            .let { part ->
                part.sumOf { it.levelTime.low.inWholeMinutes }.toDouble() /
                    part.sumOf { it.levelTime.known.inWholeMinutes }
            }

        assertTrue(focusShare(morning = true) > focusShare(morning = false))
    }

    @Test
    fun `level times and trace describe the same minutes`() {
        DemoHistory(ZoneOffset.UTC).segments(today, activities).forEach { segment ->
            assertEquals(segment.trace.windowCount.toLong(), segment.levelTime.total.inWholeMinutes)
            assertEquals(segment.duration.inWholeMinutes, segment.levelTime.total.inWholeMinutes)
        }
    }
}
