package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.testing.FakeLearningRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LearningProgressTest {
    private val repository = FakeLearningRepository()
    private val progress = LearningProgress(repository)
    private val study = ActivityId("study")
    private val reading = ActivityId("reading")

    @Test
    fun `an activity without enough sessions is still learning`() = runTest {
        repository.save(ActivityLearning(study, validSessions = 4))

        assertTrue(progress.isLearning(listOf(study)))
    }

    @Test
    fun `the hint disappears once every activity has five sessions`() = runTest {
        repository.save(ActivityLearning(study, validSessions = 5))
        repository.save(ActivityLearning(reading, validSessions = 12))

        assertFalse(progress.isLearning(listOf(study, reading)))
    }

    @Test
    fun `an activity never learned from counts as learning`() = runTest {
        assertTrue(progress.isLearning(listOf(reading)))
    }

    @Test
    fun `no activities means nothing to learn`() = runTest {
        assertFalse(progress.isLearning(emptyList()))
    }
}
