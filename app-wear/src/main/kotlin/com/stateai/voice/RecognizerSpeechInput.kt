package com.stateai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.stateai.domain.voice.VoiceFailure
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * One request through Android's [SpeechRecognizer], in Spanish. With [onDevice] it uses the
 * on-device recognizer (audio stays on the watch); otherwise the system one, which may use a server
 * and is only chosen after consent. Audio is never stored by the app; only the text comes back.
 */
class RecognizerSpeechInput(private val context: Context, private val onDevice: Boolean) : SpeechInput {
    override suspend fun listen(): SpeechResult = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val recognizer = if (onDevice) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
            fun finish(result: SpeechResult) {
                recognizer.destroy()
                if (continuation.isActive) continuation.resume(result)
            }
            recognizer.setRecognitionListener(
                Listener(
                    onText = { text ->
                        finish(
                            text?.let(SpeechResult::Heard) ?: SpeechResult.Failed(VoiceFailure.SILENCE),
                        )
                    },
                    onError = { error -> finish(SpeechResult.Failed(recognizerFailure(error))) },
                ),
            )
            recognizer.startListening(recognitionIntent(onDevice))
            continuation.invokeOnCancellation {
                recognizer.cancel()
                recognizer.destroy()
            }
        }
    }

    private class Listener(val onText: (String?) -> Unit, val onError: (Int) -> Unit) : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val best = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            onText(best?.takeIf { it.isNotBlank() })
        }

        override fun onError(error: Int) = onError.invoke(error)

        override fun onReadyForSpeech(params: Bundle?) = Unit

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onPartialResults(partialResults: Bundle?) = Unit

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    companion object {
        /** The watch's language when it is Spanish (e.g. es-CO), otherwise neutral Spanish. */
        fun spanishTag(): String = Locale.getDefault().takeIf { it.language == "es" }?.toLanguageTag() ?: "es-ES"

        private fun recognitionIntent(onDevice: Boolean) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, spanishTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, onDevice)
        }
    }
}
