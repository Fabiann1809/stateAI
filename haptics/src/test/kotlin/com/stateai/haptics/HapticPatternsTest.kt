package com.stateai.haptics

import com.stateai.domain.haptics.HapticEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class HapticPatternsTest {
    @Test
    fun `block start is one short pulse`() {
        val waveform = HapticPatterns.waveformFor(HapticEvent.BLOCK_START)

        assertEquals(1, waveform.pulseCount)
        assertTrue(waveform.totalMillis <= 150)
    }

    @Test
    fun `suggested pause is one long soft pulse`() {
        val waveform = HapticPatterns.waveformFor(HapticEvent.PAUSE_SUGGESTED)

        assertEquals(1, waveform.pulseCount)
        assertTrue(waveform.totalMillis >= 500)
        assertTrue(waveform.amplitudes.max() < 128)
    }

    @Test
    fun `overload alert is two short pulses`() {
        assertEquals(2, HapticPatterns.waveformFor(HapticEvent.OVERLOAD_ALERT).pulseCount)
    }

    @Test
    fun `one breath lasts ten seconds, four to inhale and six to exhale`() {
        val waveform = HapticPatterns.waveformFor(HapticEvent.BREATHE)

        assertEquals(10_000L, waveform.totalMillis)
        assertEquals(5, waveform.pulseCount)
    }

    @ParameterizedTest
    @EnumSource(HapticEvent::class)
    fun `every waveform starts without delay and uses valid amplitudes`(event: HapticEvent) {
        val waveform = HapticPatterns.waveformFor(event)

        assertEquals(0L, waveform.timings.first())
        assertTrue(waveform.amplitudes.all { it in 0..255 })
    }
}
