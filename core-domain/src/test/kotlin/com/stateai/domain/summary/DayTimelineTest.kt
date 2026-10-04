package com.stateai.domain.summary

import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.state.DisplayState
import com.stateai.domain.testing.testSegment
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DayTimelineTest {
    private val start = Instant.parse("2026-10-07T09:00:00Z")

    @Test
    fun `each block of ten minutes keeps its most frequent state`() {
        val trace = LevelTrace("LLLLLLLMMM" + "MMMMMMLLLL" + "--HH")
        val points = DayTimeline().points(listOf(testSegment(start = start, duration = 24.minutes, trace = trace)))

        val expected = listOf(DisplayState.FOCUSED, DisplayState.NORMAL, DisplayState.OVERLOADED)
        assertEquals(expected, points.map { it.state })
        assertEquals(start.plusSeconds(5 * 60), points.first().time)
        assertEquals(start.plusSeconds(22 * 60), points.last().time)
    }

    @Test
    fun `ties go to the more activated state and empty blocks have no state`() {
        val trace = LevelTrace("LLLLLHHHHH" + "----------")
        val points = DayTimeline().points(listOf(testSegment(start = start, duration = 20.minutes, trace = trace)))

        assertEquals(listOf(DisplayState.OVERLOADED, null), points.map { it.state })
    }

    @Test
    fun `segments are joined in time order`() {
        val later = testSegment(id = "later", start = start.plusSeconds(7200), duration = 10.minutes)
        val earlier = testSegment(id = "earlier", start = start, duration = 10.minutes)

        val points = DayTimeline().points(listOf(later, earlier))

        assertEquals(listOf(start.plusSeconds(300), start.plusSeconds(7500)), points.map { it.time })
    }
}
