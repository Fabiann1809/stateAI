package com.stateai.domain.sensing

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SensorSampleTest {
    private val now = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `heart rate may be missing`() {
        assertNull(SensorSample(now, heartRateBpm = null, movement = 0.1).heartRateBpm)
    }

    @Test
    fun `rejects non positive heart rate`() {
        assertThrows<IllegalArgumentException> { SensorSample(now, heartRateBpm = 0.0, movement = 0.1) }
    }

    @Test
    fun `rejects negative movement`() {
        assertThrows<IllegalArgumentException> { SensorSample(now, heartRateBpm = 70.0, movement = -1.0) }
    }
}
