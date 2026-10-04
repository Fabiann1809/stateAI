package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.testing.FakeLearningRepository
import com.stateai.domain.testing.testSegment
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FeedbackSensitivityLearnerTest {
    private val repository = FakeLearningRepository()
    private val learner = FeedbackSensitivityLearner(repository)
    private val id = ActivityId("activity")
    private val mostlyFocused = testSegment(levelTime = LevelDurations(low = 30.minutes, medium = 10.minutes))
    private val mostlyLoaded = testSegment(levelTime = LevelDurations(low = 10.minutes, high = 30.minutes))

    @Test
    fun `bad feeling after an estimated focused session makes the activity more sensitive`() = runTest {
        learner.learn(mostlyFocused, Feedback.BAD)

        assertEquals(-0.02, adjustment(), 1e-9)
    }

    @Test
    fun `good feeling after an estimated loaded session makes it less sensitive`() = runTest {
        learner.learn(mostlyLoaded, Feedback.GOOD)

        assertEquals(0.02, adjustment(), 1e-9)
    }

    @Test
    fun `feedback that agrees with the estimate changes nothing`() = runTest {
        learner.learn(mostlyFocused, Feedback.GOOD)
        learner.learn(mostlyLoaded, Feedback.BAD)

        assertNull(repository.stored[id])
    }

    @Test
    fun `a single feedback never changes sensitivity abruptly`() = runTest {
        learner.learn(mostlyFocused, Feedback.BAD)

        assertEquals(0.02, -adjustment(), 1e-9)
    }

    @Test
    fun `repeated feedback is bounded`() = runTest {
        repeat(50) { learner.learn(mostlyFocused, Feedback.BAD) }

        assertEquals(-0.2, adjustment(), 1e-9)
    }

    private fun adjustment() = repository.stored.getValue(id).sensitivityAdjustment
}
