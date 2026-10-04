package com.stateai.voice

import com.stateai.domain.voice.VoiceFailure

/** What one listening attempt produced. Only text is kept, and only while it is interpreted. */
sealed interface SpeechResult {
    data class Heard(val text: String) : SpeechResult

    data class Failed(val failure: VoiceFailure) : SpeechResult
}

/** Captures one spoken request. Listening starts on [listen] and stops when it returns; never continuous. */
fun interface SpeechInput {
    suspend fun listen(): SpeechResult
}
