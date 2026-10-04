package com.stateai.ml

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.model.LevelPrediction
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on the emulator: the bundled TFLite model loads and classifies windows. */
@RunWith(AndroidJUnit4::class)
class TfLiteLevelModelTest {
    private val model = TfLiteLevelModel(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun returnsOneProbabilityPerLevel() {
        val probabilities = model.probabilities(floatArrayOf(0f, 1f, 1f, 0.05f, 0f, 10f))

        assertEquals(ActivationLevel.entries.size, probabilities.size)
        assertEquals(1f, probabilities.sum(), 1e-4f)
    }

    @Test
    fun calmWindowIsLowAndLoadedWindowIsHigh() {
        val calm = LevelPrediction.from(model.probabilities(floatArrayOf(-2f, 1f, 1f, 0.05f, 0f, 10f)))
        val loaded = LevelPrediction.from(model.probabilities(floatArrayOf(16f, 2f, 1.5f, 0.12f, 1f, 30f)))

        assertEquals(ActivationLevel.LOW, calm.level)
        assertEquals(ActivationLevel.HIGH, loaded.level)
    }
}
