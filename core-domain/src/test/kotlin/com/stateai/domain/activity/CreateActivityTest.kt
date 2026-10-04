package com.stateai.domain.activity

import com.stateai.domain.testing.FakeActivityRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CreateActivityTest {
    private val repository = FakeActivityRepository()
    private var nextId = 0
    private val createActivity = CreateActivity(repository) { ActivityId("${nextId++}") }

    @Test
    fun `creates an activity with a normalized name`() = runTest {
        val result = createActivity(ActivityCategory.STUDY, "  Bases de Datos ")

        val created = assertInstanceOf(CreateActivityResult.Created::class.java, result).activity
        assertEquals("bases de datos", created.name?.normalized)
        assertEquals(1, repository.countActive())
    }

    @Test
    fun `reuses the existing activity for an equivalent name`() = runTest {
        val first = createActivity(ActivityCategory.STUDY, "BD") as CreateActivityResult.Created

        val second = createActivity(ActivityCategory.STUDY, "bd ")

        assertEquals(CreateActivityResult.Existing(first.activity), second)
        assertEquals(1, repository.countActive())
    }

    @Test
    fun `blank name creates the category activity`() = runTest {
        val result = createActivity(ActivityCategory.READING, "   ") as CreateActivityResult.Created

        assertNull(result.activity.name)
    }

    @Test
    fun `refuses a new activity when the limit is reached`() = runTest {
        repeat(ActivityLimits.MAX_ACTIVE) { createActivity(ActivityCategory.STUDY, "subject $it") }

        assertEquals(CreateActivityResult.LimitReached, createActivity(ActivityCategory.STUDY, "one more"))
    }

    @Test
    fun `an existing activity is returned even when the limit is reached`() = runTest {
        repeat(ActivityLimits.MAX_ACTIVE) { createActivity(ActivityCategory.STUDY, "subject $it") }

        assertInstanceOf(CreateActivityResult.Existing::class.java, createActivity(ActivityCategory.STUDY, "SUBJECT 0"))
    }
}
