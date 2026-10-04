package com.stateai.domain.learning

import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.testing.testSegment
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FocusProfileBuilderTest {
    private val builder = FocusProfileBuilder(ZoneOffset.UTC)

    @Test
    fun `computes the focus share of each hour across days`() {
        // Mornings (09:xx) fully focused on three days; afternoons (15:xx) half focused.
        val segments = (4..6).flatMap { day ->
            listOf(
                session("2026-10-0${day}T09:00:00Z", "L".repeat(30)),
                session("2026-10-0${day}T15:00:00Z", "LM".repeat(15)),
            )
        }

        val profile = builder.build(segments)

        assertEquals(1.0, profile.byHour.getValue(9).focusShare, 1e-9)
        assertEquals(0.5, profile.byHour.getValue(15).focusShare, 1e-9)
        assertEquals(3, profile.byHour.getValue(9).days)
        assertEquals(0.75, profile.overallFocusShare, 1e-9)
    }

    @Test
    fun `separates the same hour on different weekdays`() {
        val sunday = session("2026-10-04T09:00:00Z", "L".repeat(10))
        val monday = session("2026-10-05T09:00:00Z", "M".repeat(10))

        val profile = builder.build(listOf(sunday, monday))

        assertEquals(1.0, profile.bySlot.getValue(TimeSlot(DayOfWeek.SUNDAY, 9)).focusShare, 1e-9)
        assertEquals(0.0, profile.bySlot.getValue(TimeSlot(DayOfWeek.MONDAY, 9)).focusShare, 1e-9)
    }

    @Test
    fun `windows are attributed to the hour they happened in`() {
        val crossing = session("2026-10-04T09:50:00Z", "L".repeat(20))

        val profile = builder.build(listOf(crossing))

        assertEquals(9, profile.byHour.getValue(9).windows)
        assertEquals(11, profile.byHour.getValue(10).windows)
    }

    @Test
    fun `windows without an estimate are ignored`() {
        val profile = builder.build(listOf(session("2026-10-04T09:00:00Z", "--LL")))

        assertEquals(2, profile.byHour.getValue(9).windows)
    }

    @Test
    fun `no data gives an empty profile`() {
        assertTrue(builder.build(emptyList()).byHour.isEmpty())
    }

    private fun session(start: String, trace: String) = testSegment(
        id = start,
        start = Instant.parse(start),
        duration = trace.length.minutes,
        trace = LevelTrace(trace),
    )
}
