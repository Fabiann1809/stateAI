package com.stateai.domain.cycles

import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.testSegment
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveCycleTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-07T18:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `few sessions give no clear pattern and count the days they cover`() = runTest {
        val repository = FakeSegmentRepository()
        repository.save(testSegment(id = "a", start = Instant.parse("2026-10-06T09:00:00Z")))
        repository.save(testSegment(id = "b", start = Instant.parse("2026-10-07T09:00:00Z")))
        repository.save(testSegment(id = "c", start = Instant.parse("2026-10-07T15:00:00Z")))

        val insight = ObserveCycle(repository, clock, dispatcher = StandardTestDispatcher(testScheduler))().first()

        assertEquals(CycleResult.NoClearPattern(CycleResult.Reason.NOT_ENOUGH_DATA), insight.result)
        assertEquals(2, insight.days)
    }
}
