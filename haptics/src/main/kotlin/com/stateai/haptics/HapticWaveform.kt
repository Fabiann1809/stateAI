package com.stateai.haptics

/**
 * Vibration waveform in Android's format: [timings] alternate off/on segments in milliseconds,
 * starting with an off segment; [amplitudes] go from 0 (off) to 255 for each segment.
 */
class HapticWaveform(val timings: LongArray, val amplitudes: IntArray) {
    init {
        require(timings.size == amplitudes.size) { "Each timing needs an amplitude" }
    }

    val totalMillis: Long get() = timings.sum()

    /** Number of segments that actually vibrate. */
    val pulseCount: Int get() = amplitudes.count { it > 0 }
}
