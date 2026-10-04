package com.stateai.domain.learning

import com.stateai.domain.baseline.BaselineKeeper
import com.stateai.domain.baseline.BaselineRepository
import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.testing.testSegment
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaselineLearnerTest {
    private val repository = object : BaselineRepository {
        var stored: UserBaseline? = UserBaseline(65.0, 1.0, Instant.EPOCH)

        override suspend fun load() = stored

        override suspend fun save(baseline: UserBaseline) {
            stored = baseline
        }
    }
    private val learner = BaselineLearner(BaselineKeeper(repository))

    @Test
    fun `one session moves the baseline a fifth of the way`() = runTest {
        learner.learn(testSegment(calmHeartRate = 70.0))

        assertEquals(66.0, repository.stored!!.restingHeartRate, 1e-9)
    }

    @Test
    fun `converges to new calm data over sessions`() = runTest {
        repeat(25) { learner.learn(testSegment(calmHeartRate = 70.0)) }

        assertTrue(repository.stored!!.restingHeartRate > 69.9)
    }

    @Test
    fun `sessions without calm windows leave the baseline alone`() = runTest {
        learner.learn(testSegment(calmHeartRate = null))

        assertEquals(65.0, repository.stored!!.restingHeartRate, 1e-9)
    }

    @Test
    fun `nothing is learned before calibration`() = runTest {
        repository.stored = null

        learner.learn(testSegment(calmHeartRate = 70.0))

        assertNull(repository.stored)
    }
}
