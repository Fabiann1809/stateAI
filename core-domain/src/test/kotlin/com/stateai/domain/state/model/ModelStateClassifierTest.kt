package com.stateai.domain.state.model

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.state.ActivationLevel
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModelStateClassifierTest {
    private val baseline = UserBaseline(65.0, 1.0, Instant.EPOCH)
    private val study = DefaultCategoryProfiles.STUDY

    @Test
    fun `uses the level the model finds most likely`() {
        val classifier = ModelStateClassifier({ floatArrayOf(0.1f, 0.2f, 0.7f) })

        assertEquals(ActivationLevel.HIGH, classifier.classify(window(66.0), baseline, study, 5.minutes)?.level)
    }

    @Test
    fun `restlessness still comes from the rules`() {
        val classifier = ModelStateClassifier({ floatArrayOf(0.8f, 0.1f, 0.1f) })

        assertTrue(classifier.classify(window(66.0, fidgets = 8), baseline, study, 20.minutes)!!.restless)
    }

    @Test
    fun `passes the inputs in the trained order`() {
        val received = mutableListOf<FloatArray>()
        val model = LevelModel { inputs -> floatArrayOf(1f, 0f, 0f).also { received += inputs } }

        ModelStateClassifier(model).classify(window(70.0), baseline, study, 3.minutes)

        assertEquals(5f, received.single()[0])
        assertEquals(3f, received.single()[5])
    }

    @Test
    fun `no heart rate gives no estimate`() {
        assertNull(
            ModelStateClassifier({
                floatArrayOf(1f, 0f, 0f)
            }).classify(window(null), baseline, study, 1.minutes),
        )
    }

    private fun window(heartRate: Double?, fidgets: Int = 0) = FeatureWindow(
        Instant.EPOCH, Instant.EPOCH.plusSeconds(180), 180, if (heartRate == null) 0 else 180,
        heartRate, 1.0, 1.0, 0.05, fidgets, 0.0,
    )
}
