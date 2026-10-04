package com.stateai.ui.voice

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.data.memory.InMemoryVoiceConsentRepository
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.voice.SpeechRoute
import com.stateai.domain.voice.VoiceFailure
import com.stateai.voice.SpeechAvailability
import com.stateai.voice.SpeechInput
import com.stateai.voice.SpeechResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val activities = InMemoryActivityRepository()
    private val consent = InMemoryVoiceConsentRepository()
    private var nextId = 0
    private var listenedHaptics = 0

    @BeforeEach
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `without any recognizer a release build falls back to the list`() = runTest(dispatcher) {
        val viewModel = viewModel(onDevice = false, system = false, allowText = false)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.NO_RECOGNIZER), viewModel.uiState.value)
    }

    @Test
    fun `the emulator types instead of speaking in debug builds`() = runTest(dispatcher) {
        val viewModel = viewModel(onDevice = false, system = false, allowText = true)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Ready(SpeechRoute.TEXT), viewModel.uiState.value)
    }

    @Test
    fun `the system recognizer asks consent, and declining falls back`() = runTest(dispatcher) {
        val viewModel = viewModel(onDevice = false, system = true)
        advanceUntilIdle()
        assertEquals(VoiceUiState.AskConsent, viewModel.uiState.value)

        viewModel.answerConsent(accepted = false)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.NO_CONSENT), viewModel.uiState.value)
    }

    @Test
    fun `accepting consent remembers it and moves on to the microphone`() = runTest(dispatcher) {
        val viewModel = viewModel(onDevice = false, system = true, microphone = false)
        advanceUntilIdle()

        viewModel.answerConsent(accepted = true)
        advanceUntilIdle()

        assertTrue(consent.hasConsented())
        assertEquals(VoiceUiState.AskMicrophone, viewModel.uiState.value)
    }

    @Test
    fun `refusing the microphone falls back to the list`() = runTest(dispatcher) {
        val viewModel = viewModel(onDevice = true, microphone = false)
        advanceUntilIdle()
        assertEquals(VoiceUiState.AskMicrophone, viewModel.uiState.value)

        viewModel.answerMicrophone(granted = false)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.NO_PERMISSION), viewModel.uiState.value)
    }

    @Test
    fun `silence times out and falls back`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.listen({ CompletableDeferred<SpeechResult>().await() }, timed = true)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.SILENCE), viewModel.uiState.value)
        assertEquals(1, listenedHaptics)
    }

    @Test
    fun `recognizer errors are reported, such as no network`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.listen(heard(failure = VoiceFailure.NO_NETWORK), timed = true)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.NO_NETWORK), viewModel.uiState.value)
    }

    @Test
    fun `something not understood falls back`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.listen(heard("hola"), timed = true)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.NOT_UNDERSTOOD), viewModel.uiState.value)
    }

    @Test
    fun `an existing activity is confirmed and starts on its own after the countdown`() = runTest(dispatcher) {
        activities.add(Activity(ActivityId("bd"), ActivityCategory.STUDY, ActivityName.of("Bases de datos")))
        val viewModel = readyViewModel()
        viewModel.listen(heard("voy a estudiar bases de datos"), timed = true)
        advanceTimeBy(THINKING_AND_A_BIT)

        assertEquals(VoiceUiState.Confirm(ActivityCategory.STUDY, "Bases de datos", 3), viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(VoiceUiState.Starting(ActivityId("bd")), viewModel.uiState.value)
    }

    @Test
    fun `a new activity is created on confirmation`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.listen(heard("quiero repasar inglés"), timed = true)
        advanceTimeBy(THINKING_AND_A_BIT)
        viewModel.confirm()
        advanceUntilIdle()

        assertEquals(VoiceUiState.Starting(ActivityId("0")), viewModel.uiState.value)
        assertEquals(1, activities.countActive())
    }

    @Test
    fun `change opens the activity form with what was understood`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.listen(heard("quiero repasar inglés"), timed = true)
        advanceTimeBy(THINKING_AND_A_BIT)
        viewModel.change()
        advanceUntilIdle()

        assertEquals(VoiceUiState.Review(ActivityCategory.STUDY, "inglés"), viewModel.uiState.value)
        assertEquals(0, activities.countActive())
    }

    @Test
    fun `an unsure request goes to the activity form`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        viewModel.listen(heard("jardinería"), timed = true)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Review(null, "jardinería"), viewModel.uiState.value)
    }

    @Test
    fun `a new activity beyond the limit falls back`() = runTest(dispatcher) {
        repeat(ActivityLimits.MAX_ACTIVE) { index ->
            activities.add(Activity(ActivityId("a$index"), ActivityCategory.OTHER, ActivityName.of("cosa $index")))
        }
        val viewModel = readyViewModel()
        viewModel.listen(heard("quiero repasar inglés"), timed = true)
        advanceUntilIdle()

        assertEquals(VoiceUiState.Failed(VoiceFailure.ACTIVITY_LIMIT), viewModel.uiState.value)
    }

    private fun TestScope.readyViewModel(): VoiceViewModel = viewModel(onDevice = true).also { advanceUntilIdle() }

    private fun heard(text: String? = null, failure: VoiceFailure? = null) = SpeechInput {
        text?.let(SpeechResult::Heard) ?: SpeechResult.Failed(requireNotNull(failure))
    }

    private fun viewModel(
        onDevice: Boolean = false,
        system: Boolean = false,
        allowText: Boolean = false,
        microphone: Boolean = true,
    ) = VoiceViewModel(
        VoiceDependencies(
            speech = SpeechSetup(FakeAvailability(onDevice, system), consent, { microphone }, allowText),
            activities = VoiceActivities(
                activities,
                CreateActivity(activities) {
                    ActivityId("${nextId++}")
                },
                { "Lumi" },
            ),
            onListening = { listenedHaptics++ },
        ),
    )

    private class FakeAvailability(private val onDevice: Boolean, private val system: Boolean) : SpeechAvailability {
        override suspend fun onDeviceSpanish() = onDevice

        override fun system() = system
    }

    private companion object {
        const val THINKING_AND_A_BIT = 800L
    }
}
