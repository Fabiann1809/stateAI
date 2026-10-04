package com.stateai.ui.voice

import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.voice.SpeechRoute
import com.stateai.domain.voice.VoiceFailure

/** Steps of one spoken request, from the tap on the mascot to a session (or back to the list). */
sealed interface VoiceUiState {
    data object Preparing : VoiceUiState

    data object AskConsent : VoiceUiState

    data object AskMicrophone : VoiceUiState

    /** Everything is ready: the screen opens the input for [route] and calls `listen`. */
    data class Ready(val route: SpeechRoute) : VoiceUiState

    data object Listening : VoiceUiState

    data object Thinking : VoiceUiState

    /** "¿Empezamos ...?" with a countdown that starts the session on its own. */
    data class Confirm(val category: ActivityCategory, val name: String?, val secondsLeft: Int) : VoiceUiState

    /** Understood only in part: open "Nueva actividad" with this. */
    data class Review(val category: ActivityCategory?, val name: String?) : VoiceUiState

    data class Starting(val activityId: ActivityId) : VoiceUiState

    /** A short message, then back to the activity list. */
    data class Failed(val failure: VoiceFailure) : VoiceUiState
}
