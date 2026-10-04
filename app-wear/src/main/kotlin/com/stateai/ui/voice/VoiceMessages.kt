package com.stateai.ui.voice

import androidx.annotation.StringRes
import com.stateai.R
import com.stateai.domain.voice.VoiceFailure

/** Short, calm message for each failure; every one ends by pointing to the list. */
@StringRes
fun VoiceFailure.messageRes(): Int = when (this) {
    VoiceFailure.NO_PERMISSION -> R.string.voice_failure_no_permission
    VoiceFailure.NO_RECOGNIZER -> R.string.voice_failure_no_recognizer
    VoiceFailure.NO_NETWORK -> R.string.voice_failure_no_network
    VoiceFailure.SILENCE -> R.string.voice_failure_silence
    VoiceFailure.NOT_UNDERSTOOD -> R.string.voice_failure_not_understood
    VoiceFailure.NO_CONSENT -> R.string.voice_failure_no_consent
    VoiceFailure.ACTIVITY_LIMIT -> R.string.voice_failure_activity_limit
    VoiceFailure.ERROR -> R.string.voice_failure_error
}
