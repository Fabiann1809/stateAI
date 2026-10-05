package com.stateai.demo

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DemoHistoryTest {
    private val now = Instant.parse("2026-10-05T16:30:00Z")
    private val today = LocalDate.parse("2026-10-05")
    private val activities = DemoRoutine.ALL.associateWith { demo ->
        Activity(ActivityId(demo.name ?: "meetings"), demo.category, demo.name?.let(ActivityName::of))
    }
    private val history = DemoHistory(ZoneOffset.UTC)

    @Test
    fun `fourteen previous days plus today's sessions that already ended`() {
        val segments = history.segments(now, activities)
        val (todays, previous) = segments.partition { it.start.atZone(ZoneOffset.UTC).toLocalDate() == today }

        assertEquals(14 * 4, previous.size)
        assertEquals(today.minusDays(14), segments.first().start.atZone(ZoneOffset.UTC).toLocalDate())
        assertEquals(3, todays.size)
        assertTrue(segments.all { !it.end.isAfter(now) })
        assertEquals(segments.sortedBy { it.start }, segments)
    }

    @Test
    fun `a day is generated the same way whenever the demo is loaded`() {
        val morning = history.segments(Instant.parse("2026-10-05T10:30:00Z"), activities)
        val evening = history.segments(Instant.parse("2026-10-05T22:00:00Z"), activities)
        val tomorrow = history.segments(Instant.parse("2026-10-06T08:00:00Z"), activities)

        assertEquals(morning, evening.filter { it.id in morning.map { segment -> segment.id } })
        val shared = tomorrow.map { it.id }.toSet()
        assertEquals(evening.filter { it.id in shared }, tomorrow.filter { it.id in evening.map { e -> e.id } })
    }

    @Test
    fun `mornings have more focus than afternoons`() {
        val segments = history.segments(now, activities)
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
        history.segments(now, activities).forEach { segment ->
            assertEquals(segment.trace.windowCount.toLong(), segment.levelTime.total.inWholeMinutes)
            assertEquals(segment.duration.inWholeMinutes, segment.levelTime.total.inWholeMinutes)
        }
    }
}
