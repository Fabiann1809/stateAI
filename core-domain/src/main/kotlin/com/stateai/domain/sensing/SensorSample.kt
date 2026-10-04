package com.stateai.domain.sensing

import java.time.Instant

/**
 * One reading per second from the watch.
 *
 * @property heartRateBpm heart rate in beats per minute, or null when the sensor reported no new value
 *   (Health Services does not guarantee one value per second).
 * @property movement magnitude of linear acceleration (gravity removed) in m/s², averaged over the second.
 */
data class SensorSample(val timestamp: Instant, val heartRateBpm: Double?, val movement: Double) {
    init {
        require(heartRateBpm == null || heartRateBpm > 0) { "Heart rate must be positive" }
        require(movement >= 0) { "Movement magnitude cannot be negative" }
    }
}
