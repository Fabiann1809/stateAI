package com.stateai.ml

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.stateai.domain.state.model.LevelPrediction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on the emulator: the bundled TFLite model gives the probabilities of the original Python
 * model for the shared cases in `shared/parity/prediction_cases.json`.
 */
@RunWith(AndroidJUnit4::class)
class PredictionParityTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val model = TfLiteLevelModel(instrumentation.targetContext)

    @Test
    fun matchesPythonPredictionsWithinTolerance() {
        val cases = instrumentation.context.assets.open("prediction_cases.json").bufferedReader().use {
            Json.parseToJsonElement(it.readText()).jsonArray.map { case -> case.jsonObject }
        }

        cases.forEach { case ->
            val name = case.getValue("name").jsonPrimitive.content
            val probabilities = model.probabilities(case.floats("inputs"))
            val expected = case.floats("probabilities")

            val level = LevelPrediction.from(probabilities).level.name

            expected.indices.forEach { assertEquals(name, expected[it], probabilities[it], TOLERANCE) }
            assertEquals(name, case.getValue("level").jsonPrimitive.content, level)
        }
    }

    private fun JsonObject.floats(key: String) =
        getValue(key).jsonArray.map { it.jsonPrimitive.content.toFloat() }.toFloatArray()

    private companion object {
        const val TOLERANCE = 1e-4f
    }
}
