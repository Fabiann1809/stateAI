package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.DefaultCategoryProfiles
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RuleBasedClassifierTest {
    private val classifier = RuleBasedClassifier()
    private val baseline = UserBaseline(restingHeartRate = 65.0, heartRateMeanAbsDiff = 1.0, updatedAt = Instant.EPOCH)
    private val study = DefaultCategoryProfiles.STUDY

    @Test
    fun `heart rate near baseline with little movement is low activation`() {
        assertEquals(ActivationLevel.LOW, levelOf(window(heartRate = 67.0)))
    }

    @Test
    fun `heart rate well above baseline is high activation`() {
        assertEquals(ActivationLevel.HIGH, levelOf(window(heartRate = 80.0)))
    }

    @Test
    fun `moderate elevation is medium activation`() {
        assertEquals(ActivationLevel.MEDIUM, levelOf(window(heartRate = 73.0)))
    }

    @Test
    fun `movement above the category normal prevents low activation`() {
        assertEquals(ActivationLevel.MEDIUM, levelOf(window(heartRate = 66.0, movement = 0.4)))
    }

    @Test
    fun `collaborative work tolerates more movement`() {
        val level = classifier.classify(
            window(66.0, movement = 0.4),
            baseline,
            DefaultCategoryProfiles.COLLAB,
            0.minutes,
        )

        assertEquals(ActivationLevel.LOW, level?.level)
    }

    @Test
    fun `high sensitivity reacts to a smaller elevation`() {
        val reading = DefaultCategoryProfiles.READING
        val window = window(heartRate = 75.0)

        assertEquals(ActivationLevel.MEDIUM, levelOf(window))
        assertEquals(ActivationLevel.HIGH, classifier.classify(window, baseline, reading, 0.minutes)?.level)
    }

    @Test
    fun `a learned movement limit replaces the category limit`() {
        val tolerant = study.copy(movementLimit = 0.5)

        assertEquals(
            ActivationLevel.LOW,
            classifier.classify(window(66.0, movement = 0.4), baseline, tolerant, 0.minutes)?.level,
        )
    }

    @Test
    fun `a negative sensitivity adjustment reacts to smaller elevations`() {
        val sensitive = study.copy(sensitivityAdjustment = -0.2)

        assertEquals(
            ActivationLevel.HIGH,
            classifier.classify(window(heartRate = 75.0), baseline, sensitive, 0.minutes)?.level,
        )
    }

    @Test
    fun `fidgeting late in a session flags restlessness`() {
        assertTrue(classifier.classify(window(66.0, fidgets = 8), baseline, study, 20.minutes)!!.restless)
    }

    @Test
    fun `fidgeting early in a session is not restlessness`() {
        assertFalse(classifier.classify(window(66.0, fidgets = 8), baseline, study, 5.minutes)!!.restless)
    }

    @Test
    fun `no heart rate gives no estimate`() {
        assertNull(classifier.classify(window(heartRate = null), baseline, study, 0.minutes))
    }

    private fun levelOf(window: FeatureWindow) = classifier.classify(window, baseline, study, 0.minutes)?.level

    private fun window(heartRate: Double?, movement: Double = 0.05, fidgets: Int = 0) = FeatureWindow(
        start = Instant.EPOCH,
        end = Instant.EPOCH.plusSeconds(180),
        sampleCount = 180,
        heartRateCount = if (heartRate == null) 0 else 180,
        meanHeartRate = heartRate,
        heartRateStdDev = 1.0,
        heartRateMeanAbsDiff = 1.0,
        meanMovement = movement,
        fidgetCount = fidgets,
        highMovementShare = 0.0,
    )
}
