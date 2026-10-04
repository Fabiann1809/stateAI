package com.stateai.voice

import android.speech.SpeechRecognizer
import com.stateai.domain.voice.VoiceFailure

/** Maps a [SpeechRecognizer] error code to the failure shown to the person. */
fun recognizerFailure(error: Int): VoiceFailure = when (error) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceFailure.SILENCE
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
    SpeechRecognizer.ERROR_SERVER,
    SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
    -> VoiceFailure.NO_NETWORK
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceFailure.NO_PERMISSION
    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
    -> VoiceFailure.NO_RECOGNIZER
    else -> VoiceFailure.ERROR
}
