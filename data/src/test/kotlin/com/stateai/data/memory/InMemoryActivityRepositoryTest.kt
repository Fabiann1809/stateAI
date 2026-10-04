package com.stateai.data.memory

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class InMemoryActivityRepositoryTest {
    private val repository = InMemoryActivityRepository()
    private val study = Activity(ActivityId("study"), ActivityCategory.STUDY, ActivityName.of("bd"))
    private val reading = Activity(ActivityId("reading"), ActivityCategory.READING, name = null)

    @Test
    fun `never used activities keep creation order`() = runTest {
        repository.add(study)
        repository.add(reading)

        assertEquals(listOf(study, reading), repository.observeActive().first())
    }

    @Test
    fun `most recently used activity comes first`() = runTest {
        repository.add(study)
        repository.add(reading)
        repository.markUsed(study.id, Instant.parse("2026-10-04T08:00:00Z"))
        repository.markUsed(reading.id, Instant.parse("2026-10-04T09:00:00Z"))

        assertEquals(listOf(reading, study), repository.observeActive().first())
    }

    @Test
    fun `finds an activity by its normalized key`() = runTest {
        repository.add(study)

        val sameActivity = Activity(ActivityId("other"), ActivityCategory.STUDY, ActivityName.of("BD "))
        assertNotNull(repository.findByKey(sameActivity.key))
    }

    @Test
    fun `counts active activities`() = runTest {
        repository.add(study)
        repository.add(reading)

        assertEquals(2, repository.countActive())
    }
}
