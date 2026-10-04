package com.stateai.voice

import com.stateai.domain.voice.VoiceFailure

/**
 * Typed text in place of voice, for debug builds and the emulator (which has no recognizer).
 * [requestText] opens the system text input and returns what was typed, or null if dismissed.
 */
class TextSpeechInput(private val requestText: suspend () -> String?) : SpeechInput {
    override suspend fun listen(): SpeechResult =
        requestText()?.takeIf { it.isNotBlank() }?.let(SpeechResult::Heard) ?: SpeechResult.Failed(VoiceFailure.SILENCE)
}
