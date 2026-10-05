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
    private var now = Instant.parse("2026-10-05T10:30:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC

        override fun withZone(zone: java.time.ZoneId?) = this

        override fun instant() = now
    }
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
        val first = loader.load()
        assertEquals(DemoLoadResult.AlreadyLoaded, loader.load())

        assertEquals(DemoLoadResult.Loaded(57), first)
        assertEquals(57, segments.all().size)
        assertEquals(57, learned.size)
        assertEquals(4, activities.observeActive().first().size)
        assertNotNull(baseline.load())
    }

    @Test
    fun `loading again later the same day only adds the sessions that ended since`() = runTest {
        loader.load()
        now = Instant.parse("2026-10-05T22:00:00Z")

        assertEquals(DemoLoadResult.Loaded(3), loader.load())
        assertEquals(60, segments.all().size)
        assertEquals(60, learned.size)
    }

    private class MemoryBaseline : BaselineRepository {
        private var stored: UserBaseline? = null

        override suspend fun load(): UserBaseline? = stored

        override suspend fun save(baseline: UserBaseline) {
            stored = baseline
        }
    }
}
