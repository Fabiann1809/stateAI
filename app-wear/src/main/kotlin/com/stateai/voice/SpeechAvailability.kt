package com.stateai.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Which recognizers this watch has. Checked every time, since recognizers can be installed later. */
interface SpeechAvailability {
    /** An on-device recognizer with Spanish installed: nothing leaves the watch. */
    suspend fun onDeviceSpanish(): Boolean

    /** Any system recognizer (it may use a server). */
    fun system(): Boolean
}

/**
 * Android implementation. On-device Spanish is confirmed with `checkRecognitionSupport`: a language
 * that is only downloadable does not count, so the app never waits for a download.
 */
class AndroidSpeechAvailability(private val context: Context) : SpeechAvailability {
    override fun system(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override suspend fun onDeviceSpanish(): Boolean {
        val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        return supported &&
            withTimeoutOrNull(SUPPORT_TIMEOUT_MILLIS) { installedLanguages() }
                .orEmpty().any { it.startsWith(SPANISH) }
    }

    private suspend fun installedLanguages(): List<String> = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerSpeechInput.spanishTag())
            recognizer.checkRecognitionSupport(
                intent,
                context.mainExecutor,
                object : RecognitionSupportCallback {
                    override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                        recognizer.destroy()
                        continuation.resume(recognitionSupport.installedOnDeviceLanguages)
                    }

                    override fun onError(error: Int) {
                        recognizer.destroy()
                        continuation.resume(emptyList())
                    }
                },
            )
            continuation.invokeOnCancellation { recognizer.destroy() }
        }
    }

    private companion object {
        const val SPANISH = "es"
        const val SUPPORT_TIMEOUT_MILLIS = 2_000L
    }
}
