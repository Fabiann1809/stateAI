package com.stateai.haptics

import com.stateai.domain.haptics.HapticEvent

/** Waveforms of the haptic language. Pure mapping, testable without a device. */
object HapticPatterns {
    private const val OFF = 0
    private const val SHORT_PULSE_MS = 80L
    private const val STRONG = 220
    private const val SOFT = 90
    private const val DOUBLE_PULSE_GAP_MS = 140L
    private const val LONG_PULSE_MS = 600L

    private const val INHALE_MS = 4_000L
    private const val EXHALE_MS = 6_000L
    private const val INHALE_TICK_MS = 100L
    private val INHALE_AMPLITUDES = intArrayOf(40, 70, 100, 130)
    private const val EXHALE_PULSE_MS = 900L
    private const val EXHALE_AMPLITUDE = 60

    fun waveformFor(event: HapticEvent): HapticWaveform = when (event) {
        HapticEvent.BLOCK_START -> HapticWaveform(longArrayOf(0, SHORT_PULSE_MS), intArrayOf(OFF, STRONG))
        HapticEvent.PAUSE_SUGGESTED -> HapticWaveform(longArrayOf(0, LONG_PULSE_MS), intArrayOf(OFF, SOFT))
        HapticEvent.OVERLOAD_ALERT -> HapticWaveform(
            longArrayOf(0, SHORT_PULSE_MS, DOUBLE_PULSE_GAP_MS, SHORT_PULSE_MS),
            intArrayOf(OFF, STRONG, OFF, STRONG),
        )
        HapticEvent.BREATHE -> breathCycle()
    }

    /** One breath: rising ticks once per second while inhaling, then one soft pulse to exhale. */
    private fun breathCycle(): HapticWaveform {
        val tickSpacing = INHALE_MS / INHALE_AMPLITUDES.size
        val timings = mutableListOf<Long>()
        val amplitudes = mutableListOf<Int>()
        INHALE_AMPLITUDES.forEachIndexed { index, amplitude ->
            timings += if (index == 0) 0L else tickSpacing - INHALE_TICK_MS
            amplitudes += OFF
            timings += INHALE_TICK_MS
            amplitudes += amplitude
        }
        timings += tickSpacing - INHALE_TICK_MS
        amplitudes += OFF
        timings += EXHALE_PULSE_MS
        amplitudes += EXHALE_AMPLITUDE
        timings += EXHALE_MS - EXHALE_PULSE_MS
        amplitudes += OFF
        return HapticWaveform(timings.toLongArray(), amplitudes.toIntArray())
    }
}
