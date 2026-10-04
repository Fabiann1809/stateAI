package com.stateai.sensors.health

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SecondAggregatorTest {
    private val now = Instant.parse("2026-10-04T09:00:00Z")
    private val aggregator = SecondAggregator()

    @Test
    fun `uses the latest heart rate and the mean movement of the second`() {
        aggregator.onHeartRate(70.0)
        aggregator.onHeartRate(72.0)
        aggregator.onMovement(0.1)
        aggregator.onMovement(0.3)

        val sample = aggregator.tick(now)

        assertEquals(72.0, sample.heartRateBpm)
        assertEquals(0.2, sample.movement, 1e-9)
        assertEquals(now, sample.timestamp)
    }

    @Test
    fun `heart rate is missing when no value arrived since the last tick`() {
        aggregator.onHeartRate(70.0)
        aggregator.tick(now)

        assertNull(aggregator.tick(now.plusSeconds(1)).heartRateBpm)
    }

    @Test
    fun `movement is zero without readings`() {
        assertEquals(0.0, aggregator.tick(now).movement)
    }

    @Test
    fun `ignores invalid heart rates`() {
        aggregator.onHeartRate(0.0)

        assertNull(aggregator.tick(now).heartRateBpm)
    }
}
