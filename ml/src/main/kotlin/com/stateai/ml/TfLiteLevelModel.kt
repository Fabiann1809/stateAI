package com.stateai.ml

import android.content.Context
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.model.LevelModel
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter

/**
 * Runs the exported state classifier (`assets/state_classifier.tflite`) with LiteRT. The model takes
 * the six [com.stateai.domain.state.model.ModelInputs] and returns one probability per level.
 */
class TfLiteLevelModel(context: Context, assetName: String = DEFAULT_ASSET) : LevelModel {
    private val interpreter = Interpreter(load(context, assetName))

    override fun probabilities(inputs: FloatArray): FloatArray {
        val output = arrayOf(FloatArray(ActivationLevel.entries.size))
        synchronized(interpreter) { interpreter.run(arrayOf(inputs), output) }
        return output[0]
    }

    private fun load(context: Context, assetName: String): MappedByteBuffer =
        context.assets.openFd(assetName).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, descriptor.startOffset, descriptor.declaredLength)
            }
        }

    companion object {
        const val DEFAULT_ASSET = "state_classifier.tflite"
    }
}
