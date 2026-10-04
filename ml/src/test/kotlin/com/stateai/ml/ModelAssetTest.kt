package com.stateai.ml

import java.io.File
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

/** The model bundled in the app must be the one exported and verified in `ml-python`. */
class ModelAssetTest {
    @Test
    fun `bundled model is identical to the exported model`() {
        val exported = File("../ml-python/models/state_classifier.tflite").readBytes()
        val bundled = File("src/main/assets/${TfLiteLevelModel.DEFAULT_ASSET}").readBytes()

        assertArrayEquals(exported, bundled, "Copy ml-python/models/state_classifier.tflite into ml/src/main/assets")
    }
}
