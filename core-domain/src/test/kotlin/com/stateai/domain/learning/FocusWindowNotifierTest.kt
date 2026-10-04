package com.stateai.domain.learning

import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.MutableClock
import com.stateai.domain.testing.testSegment
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FocusWindowNotifierTest {
    private val segments = FakeSegmentRepository()
    private val played = mutableListOf<HapticEvent>()
    private val clock = MutableClock(Instant.parse("2026-10-10T09:10:00Z"))
    private val notifier = FocusWindowNotifier(ObserveFocusProfile(segments, clock), { played += it }, clock)

    @Test
    fun `focused mornings over five days make 9 am a focus window and pulse once`() = runTest {
        (4..8).forEach { day ->
            segments.save(session("2026-10-0${day}T09:00:00Z", "L".repeat(40)))
            segments.save(session("2026-10-0${day}T15:00:00Z", "M".repeat(40)))
        }

        assertTrue(notifier.observe().first())
        assertTrue(notifier.observe().first())
        assertEquals(listOf(HapticEvent.BLOCK_START), played)
    }

    @Test
    fun `without enough history there is no focus window`() = runTest {
        segments.save(session("2026-10-08T09:00:00Z", "L".repeat(40)))

        assertFalse(notifier.observe().first())
        assertTrue(played.isEmpty())
    }

    private fun session(start: String, trace: String) = testSegment(
        id = start,
        start = Instant.parse(start),
        duration = trace.length.minutes,
        trace = LevelTrace(trace),
    )
}
