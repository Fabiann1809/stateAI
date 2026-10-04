package com.stateai.domain.features

import com.stateai.domain.sensing.SensorSample
import java.time.Instant
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FeatureWindowStreamTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")
    private val stream = FeatureWindowStream()

    @Test
    fun `emits one window per minute`() = runTest {
        val windows = stream.windows(secondsOfSamples(count = 600)).toList()

        assertEquals(9, windows.size)
    }

    @Test
    fun `windows cover at most the last three minutes`() = runTest {
        val windows = stream.windows(secondsOfSamples(count = 600)).toList()

        assertEquals(61, windows.first().sampleCount)
        assertEquals(180, windows.last().sampleCount)
    }

    @Test
    fun `window features follow the latest samples`() = runTest {
        val samples = (0 until 500).map { second ->
            val heartRate = if (second < 200) 60.0 else 90.0
            SensorSample(start.plusSeconds(second.toLong()), heartRate, movement = 0.1)
        }

        val last = stream.windows(samples.asFlow()).toList().last()

        assertEquals(90.0, last.meanHeartRate!!, 1e-9)
    }

    private fun secondsOfSamples(count: Int) =
        (0 until count).map { SensorSample(start.plusSeconds(it.toLong()), 65.0, movement = 0.1) }.asFlow()
}
