package com.stateai.domain.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpeechRouteTest {
    @Test
    fun `the on-device recognizer is preferred over everything`() {
        assertEquals(
            SpeechRoute.ON_DEVICE,
            SpeechRoute.choose(onDevice = true, system = true, consented = false, allowText = true),
        )
    }

    @Test
    fun `the system recognizer needs consent first`() {
        assertEquals(
            SpeechRoute.ASK_CONSENT,
            SpeechRoute.choose(onDevice = false, system = true, consented = false, allowText = true),
        )
        assertEquals(
            SpeechRoute.SYSTEM,
            SpeechRoute.choose(onDevice = false, system = true, consented = true, allowText = false),
        )
    }

    @Test
    fun `without recognizers only debug builds can type, otherwise it falls back to the list`() {
        assertEquals(
            SpeechRoute.TEXT,
            SpeechRoute.choose(onDevice = false, system = false, consented = false, allowText = true),
        )
        assertEquals(
            SpeechRoute.UNAVAILABLE,
            SpeechRoute.choose(onDevice = false, system = false, consented = true, allowText = false),
        )
    }

    @Test
    fun `only voice needs the microphone`() {
        assertTrue(SpeechRoute.ON_DEVICE.needsMicrophone)
        assertTrue(SpeechRoute.SYSTEM.needsMicrophone)
        assertFalse(SpeechRoute.TEXT.needsMicrophone)
    }
}
