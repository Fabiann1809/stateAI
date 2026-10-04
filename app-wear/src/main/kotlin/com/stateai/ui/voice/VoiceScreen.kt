package com.stateai.ui.voice

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stateai.BuildConfig
import com.stateai.R
import com.stateai.di.AppContainer
import com.stateai.di.appContainer
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.voice.SpeechRoute
import com.stateai.ui.common.rememberSuspendingTextInput
import com.stateai.ui.mascot.MascotExpression
import com.stateai.voice.RecognizerSpeechInput
import com.stateai.voice.SpeechInput
import com.stateai.voice.TextSpeechInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun VoiceRoute(exits: VoiceExits) {
    val container = appContainer()
    val context = LocalContext.current
    val viewModel: VoiceViewModel = viewModel(
        factory = viewModelFactory { initializer { VoiceViewModel(voiceDependencies(container, context)) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val typed = rememberSuspendingTextInput(stringResource(R.string.voice_text_label))
    val requestMicrophone = rememberMicrophonePermissionRequest { granted -> viewModel.answerMicrophone(granted) }

    VoiceContent(
        state = state,
        onConsent = viewModel::answerConsent,
        onMicrophone = { accepted -> if (accepted) requestMicrophone() else viewModel.answerMicrophone(false) },
        onChange = viewModel::change,
        onCancel = exits.onFallback,
    )
    VoiceEffects(state, exits) { route ->
        viewModel.listen(inputFor(route, context, typed), timed = route != SpeechRoute.TEXT)
    }
}

/** What each step shows. */
@Composable
private fun VoiceContent(
    state: VoiceUiState,
    onConsent: (Boolean) -> Unit,
    onMicrophone: (Boolean) -> Unit,
    onChange: () -> Unit,
    onCancel: () -> Unit,
) {
    when (state) {
        VoiceUiState.AskConsent -> ConsentExplanation(onConsent)
        VoiceUiState.AskMicrophone -> MicrophoneExplanation(onMicrophone)
        is VoiceUiState.Ready, VoiceUiState.Listening ->
            VoiceStatus(MascotExpression.LISTENING, stringResource(R.string.voice_listening))
        VoiceUiState.Thinking -> VoiceStatus(MascotExpression.THINKING, stringResource(R.string.voice_thinking))
        is VoiceUiState.Confirm -> ConfirmCard(state.category, state.name, state.secondsLeft, onChange, onCancel)
        is VoiceUiState.Failed -> VoiceStatus(MascotExpression.REST, stringResource(state.failure.messageRes()))
        else -> VoiceStatus(MascotExpression.REST, "")
    }
}

/** What each step does once: listen, leave for a session or "Nueva actividad", or fall back. */
@Composable
private fun VoiceEffects(state: VoiceUiState, exits: VoiceExits, listen: (SpeechRoute) -> Unit) {
    LaunchedEffect(state) {
        when (state) {
            is VoiceUiState.Ready -> listen(state.route)
            is VoiceUiState.Review -> exits.onReview(state.category, state.name)
            is VoiceUiState.Starting -> exits.onStart(state.activityId)
            is VoiceUiState.Failed -> {
                delay(FAILURE_MESSAGE_MILLIS)
                exits.onFallback()
            }
            else -> Unit
        }
    }
}

@Composable
private fun ConsentExplanation(onAnswer: (Boolean) -> Unit) = VoiceExplanation(
    title = stringResource(R.string.voice_consent_title),
    body = stringResource(R.string.voice_consent_body),
    accept = stringResource(R.string.voice_consent_accept),
    decline = stringResource(R.string.voice_use_list),
    onAnswer = onAnswer,
)

@Composable
private fun MicrophoneExplanation(onAnswer: (Boolean) -> Unit) = VoiceExplanation(
    title = stringResource(R.string.voice_mic_title),
    body = stringResource(R.string.voice_mic_body),
    accept = stringResource(R.string.voice_mic_accept),
    decline = stringResource(R.string.voice_use_list),
    onAnswer = onAnswer,
)

private fun inputFor(route: SpeechRoute, context: Context, typed: suspend () -> String?): SpeechInput = when (route) {
    SpeechRoute.ON_DEVICE -> RecognizerSpeechInput(context, onDevice = true)
    SpeechRoute.SYSTEM -> RecognizerSpeechInput(context, onDevice = false)
    else -> TextSpeechInput(typed)
}

private fun voiceDependencies(container: AppContainer, context: Context) = VoiceDependencies(
    speech = SpeechSetup(
        availability = container.speechAvailability,
        consent = container.voiceConsent,
        hasMicrophone = { context.hasMicrophonePermission() },
        allowText = BuildConfig.DEBUG,
    ),
    activities = VoiceActivities(
        repository = container.activityRepository,
        create = container.createActivity,
        mascotName = { container.mascotRepository.observeName().first() },
    ),
    onListening = { container.haptics.sessionPlayer.play(HapticEvent.BLOCK_START) },
)

private const val FAILURE_MESSAGE_MILLIS = 2_500L
