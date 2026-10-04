package com.stateai.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.activity.CreateActivityResult
import com.stateai.domain.voice.ActivityMatcher
import com.stateai.domain.voice.IntentParser
import com.stateai.domain.voice.SpeechRoute
import com.stateai.domain.voice.VoiceConsentRepository
import com.stateai.domain.voice.VoiceFailure
import com.stateai.domain.voice.VoiceMatch
import com.stateai.voice.SpeechAvailability
import com.stateai.voice.SpeechInput
import com.stateai.voice.SpeechResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** How the watch can listen: recognizers, consent, microphone permission and the debug text input. */
class SpeechSetup(
    val availability: SpeechAvailability,
    val consent: VoiceConsentRepository,
    val hasMicrophone: () -> Boolean,
    val allowText: Boolean,
)

/** What a request is matched against and how a new activity is created. */
class VoiceActivities(
    val repository: ActivityRepository,
    val create: CreateActivity,
    val mascotName: suspend () -> String?,
    val matcher: ActivityMatcher = ActivityMatcher(),
)

/** Everything the voice flow needs, injected so it runs on the JVM in tests. */
class VoiceDependencies(val speech: SpeechSetup, val activities: VoiceActivities, val onListening: () -> Unit)

/**
 * One spoken request: choose how to listen (asking consent or the microphone when needed), listen
 * once with a timeout, interpret the text, and confirm before starting. Every failure ends in
 * [VoiceUiState.Failed], which the screen turns into a short message and the activity list.
 */
class VoiceViewModel(private val deps: VoiceDependencies) : ViewModel() {
    private val state = MutableStateFlow<VoiceUiState>(VoiceUiState.Preparing)
    val uiState: StateFlow<VoiceUiState> = state.asStateFlow()
    private var pending: VoiceMatch? = null
    private var countdown: Job? = null

    init {
        viewModelScope.launch { prepare() }
    }

    fun answerConsent(accepted: Boolean) = viewModelScope.launch {
        if (!accepted) return@launch fail(VoiceFailure.NO_CONSENT)
        deps.speech.consent.consent()
        prepare()
    }

    fun answerMicrophone(granted: Boolean) = viewModelScope.launch {
        if (granted) prepare() else fail(VoiceFailure.NO_PERMISSION)
    }

    /** Listens once. Voice has a timeout for silence; typed text (debug) waits for the keyboard. */
    fun listen(input: SpeechInput, timed: Boolean) = viewModelScope.launch {
        state.value = VoiceUiState.Listening
        deps.onListening()
        val result = if (timed) withTimeoutOrNull(LISTEN_TIMEOUT_MILLIS) { input.listen() } else input.listen()
        when (result) {
            is SpeechResult.Heard -> interpret(result.text)
            is SpeechResult.Failed -> fail(result.failure)
            null -> fail(VoiceFailure.SILENCE)
        }
    }

    fun confirm() = viewModelScope.launch {
        countdown?.cancel()
        when (val match = pending) {
            is VoiceMatch.Existing -> state.value = VoiceUiState.Starting(match.activity.id)
            is VoiceMatch.New -> create(match)
            else -> fail(VoiceFailure.ERROR)
        }
    }

    /** "Cambiar": go to "Nueva actividad" with what was understood. */
    fun change() {
        countdown?.cancel()
        val current = state.value as? VoiceUiState.Confirm ?: return
        state.value = VoiceUiState.Review(current.category, current.name)
    }

    private suspend fun prepare() {
        val speech = deps.speech
        val route = SpeechRoute.choose(
            onDevice = speech.availability.onDeviceSpanish(),
            system = speech.availability.system(),
            consented = speech.consent.hasConsented(),
            allowText = speech.allowText,
        )
        state.value = when {
            route == SpeechRoute.UNAVAILABLE -> VoiceUiState.Failed(VoiceFailure.NO_RECOGNIZER)
            route == SpeechRoute.ASK_CONSENT -> VoiceUiState.AskConsent
            route.needsMicrophone && !speech.hasMicrophone() -> VoiceUiState.AskMicrophone
            else -> VoiceUiState.Ready(route)
        }
    }

    private suspend fun interpret(text: String) {
        state.value = VoiceUiState.Thinking
        delay(THINKING_MILLIS)
        val activities = deps.activities
        val intent = IntentParser(activities.mascotName()).parse(text)
        when (val match = activities.matcher.match(intent, activities.repository.observeActive().first())) {
            is VoiceMatch.Existing -> askToStart(match, match.activity.category, match.activity.name?.display)
            is VoiceMatch.New -> askToStart(match, match.category, match.name)
            is VoiceMatch.Review -> state.value = VoiceUiState.Review(match.category, match.name)
            VoiceMatch.NotUnderstood -> fail(VoiceFailure.NOT_UNDERSTOOD)
        }
    }

    private fun askToStart(match: VoiceMatch, category: ActivityCategory, name: String?) {
        pending = match
        countdown = viewModelScope.launch {
            for (seconds in COUNTDOWN_SECONDS downTo 1) {
                state.value = VoiceUiState.Confirm(category, name, seconds)
                delay(SECOND_MILLIS)
            }
            confirm()
        }
    }

    private suspend fun create(match: VoiceMatch.New) {
        state.value = when (val result = deps.activities.create(match.category, match.name)) {
            is CreateActivityResult.Created -> VoiceUiState.Starting(result.activity.id)
            is CreateActivityResult.Existing -> VoiceUiState.Starting(result.activity.id)
            CreateActivityResult.LimitReached -> VoiceUiState.Failed(VoiceFailure.ACTIVITY_LIMIT)
        }
    }

    private fun fail(failure: VoiceFailure) {
        countdown?.cancel()
        state.value = VoiceUiState.Failed(failure)
    }

    private companion object {
        const val LISTEN_TIMEOUT_MILLIS = 12_000L
        const val THINKING_MILLIS = 700L
        const val COUNTDOWN_SECONDS = 3
        const val SECOND_MILLIS = 1_000L
    }
}
