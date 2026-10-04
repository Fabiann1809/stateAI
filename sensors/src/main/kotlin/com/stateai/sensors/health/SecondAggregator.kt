package com.stateai.sensors.health

import com.stateai.domain.sensing.SensorSample
import java.time.Instant

/**
 * Collects raw readings between ticks and turns them into one [SensorSample] per tick:
 * the latest heart rate received since the previous tick (or null) and the mean movement.
 * Thread-safe, because sensor callbacks and the ticker run on different threads.
 */
class SecondAggregator {
    private val lock = Any()
    private var latestHeartRate: Double? = null
    private var movementSum = 0.0
    private var movementCount = 0

    fun onHeartRate(bpm: Double) = synchronized(lock) {
        if (bpm > 0) latestHeartRate = bpm
    }

    fun onMovement(magnitude: Double) = synchronized(lock) {
        movementSum += magnitude
        movementCount++
    }

    fun tick(now: Instant): SensorSample = synchronized(lock) {
        val movement = if (movementCount == 0) 0.0 else movementSum / movementCount
        val sample = SensorSample(now, latestHeartRate, movement.coerceAtLeast(0.0))
        latestHeartRate = null
        movementSum = 0.0
        movementCount = 0
        sample
    }
}
