package com.stateai.domain.baseline

import com.stateai.domain.sensing.SensorSample
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BaselineKeeperTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")
    private val repository = object : BaselineRepository {
        var stored: UserBaseline? = null

        override suspend fun load() = stored

        override suspend fun save(baseline: UserBaseline) {
            stored = baseline
        }
    }

    @Test
    fun `calibrates once and saves the baseline`() = runTest {
        val keeper = BaselineKeeper(repository)

        val last = (0..120).map { keeper.calibrate(sample(it)) }.last()

        assertInstanceOf(CalibrationProgress.Done::class.java, last)
        assertNotNull(repository.stored)
    }

    @Test
    fun `a later session reuses the stored baseline without calibrating`() = runTest {
        val firstSession = BaselineKeeper(repository)
        (0..120).forEach { firstSession.calibrate(sample(it)) }

        val nextSession = BaselineKeeper(repository)

        assertNull(nextSession.calibrate(sample(0)))
        assertEquals(repository.stored, nextSession.current())
    }

    private fun sample(second: Int) = SensorSample(start.plusSeconds(second.toLong()), 66.0, movement = 0.1)
}
