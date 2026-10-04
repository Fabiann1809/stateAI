package com.stateai.domain.state.model

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.state.ActivationLevel
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ModelInputsTest {
    private val baseline = UserBaseline(65.0, 1.0, Instant.EPOCH)

    @Test
    fun `builds inputs in the order the Python model was trained with`() {
        val inputs = ModelInputs.of(window(meanHeartRate = 72.0), baseline, 12.minutes)

        assertArrayEquals(floatArrayOf(7f, 1.5f, 0.8f, 0.12f, 3f, 12f), inputs)
        assertEquals(6, ModelInputs.NAMES.size)
    }

    @Test
    fun `no heart rate gives no inputs`() {
        assertNull(ModelInputs.of(window(meanHeartRate = null), baseline, 1.minutes))
    }

    @Test
    fun `prediction picks the most likely level`() {
        assertEquals(LevelPrediction(ActivationLevel.HIGH, 0.7f), LevelPrediction.from(floatArrayOf(0.1f, 0.2f, 0.7f)))
    }

    private fun window(meanHeartRate: Double?) = FeatureWindow(
        start = Instant.EPOCH,
        end = Instant.EPOCH.plusSeconds(180),
        sampleCount = 180,
        heartRateCount = 180,
        meanHeartRate = meanHeartRate,
        heartRateStdDev = 1.5,
        heartRateMeanAbsDiff = 0.8,
        meanMovement = 0.12,
        fidgetCount = 3,
        highMovementShare = 0.0,
    )
}
