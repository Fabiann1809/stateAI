package com.stateai.demo

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.data.memory.InMemorySegmentRepository
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.baseline.BaselineRepository
import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.learning.SegmentLearner
import com.stateai.domain.segment.Segment
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DemoLoaderTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-05T10:00:00Z"), ZoneOffset.UTC)
    private val activities = InMemoryActivityRepository()
    private val segments = InMemorySegmentRepository()
    private val baseline = MemoryBaseline()
    private val learned = mutableListOf<Segment>()
    private var nextId = 0
    private val loader = DemoLoader(
        DemoTargets(
            createActivity = CreateActivity(activities) { ActivityId("a${nextId++}") },
            activities = activities,
            segments = segments,
            baseline = baseline,
            learners = listOf(SegmentLearner { segment -> learned += segment }),
        ),
        clock,
    )

    @Test
    fun `loads the history through the learners, once`() = runTest {
        assertEquals(DemoLoadResult.Loaded(56), loader.load())
        assertEquals(DemoLoadResult.AlreadyLoaded, loader.load())

        assertEquals(56, segments.all().size)
        assertEquals(56, learned.size)
        assertEquals(4, activities.observeActive().first().size)
        assertNotNull(baseline.load())
    }

    private class MemoryBaseline : BaselineRepository {
        private var stored: UserBaseline? = null

        override suspend fun load(): UserBaseline? = stored

        override suspend fun save(baseline: UserBaseline) {
            stored = baseline
        }
    }
}
