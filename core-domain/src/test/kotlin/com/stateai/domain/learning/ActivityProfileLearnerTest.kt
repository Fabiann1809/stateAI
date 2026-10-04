package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.testing.FakeLearningRepository
import com.stateai.domain.testing.testSegment
import java.util.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ActivityProfileLearnerTest {
    private val repository = FakeLearningRepository()
    private val learner = ActivityProfileLearner(repository)
    private val activityId = ActivityId("activity")

    @Test
    fun `learned block and movement approach the synthetic user's values`() = runTest {
        // Synthetic user: natural sessions around 55 minutes with movement around 0.12 m/s².
        val random = Random(3)
        repeat(20) {
            val minutes = 55 + random.nextGaussian() * 5
            val movement = 0.12 + random.nextGaussian() * 0.02
            learner.learn(testSegment(duration = (minutes * 60).seconds, cleanMovement = movement))
        }

        val learned = repository.stored.getValue(activityId)
        assertEquals(20, learned.validSessions)
        assertEquals(55.0, learned.meanDuration!!.inWholeSeconds / 60.0, 3.0)
        assertEquals(0.12, learned.meanMovement!!, 0.01)
    }

    @Test
    fun `invalid sessions do not count`() = runTest {
        learner.learn(testSegment(duration = 5.minutes, cleanMovement = 0.1))

        assertNull(repository.stored[activityId])
    }

    @Test
    fun `running means weigh every session equally`() = runTest {
        learner.learn(testSegment(duration = 40.minutes, cleanMovement = 0.1))
        learner.learn(testSegment(duration = 60.minutes, cleanMovement = 0.3))

        val learned = repository.stored.getValue(activityId)
        assertEquals(50.minutes, learned.meanDuration)
        assertEquals(0.2, learned.meanMovement!!, 1e-9)
    }
}
