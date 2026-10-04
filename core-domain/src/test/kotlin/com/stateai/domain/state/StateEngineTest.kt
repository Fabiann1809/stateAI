package com.stateai.domain.state

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.profile.DefaultCategoryProfiles
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class StateEngineTest {
    private val engine = StateEngine()
    private val baseline = UserBaseline(restingHeartRate = 65.0, heartRateMeanAbsDiff = 1.0, updatedAt = Instant.EPOCH)

    @Test
    fun `walking raises heart rate but does not trigger high activation`() {
        engine.feed(window(heartRate = 66.0))

        val afterWalking = engine.feed(window(heartRate = 95.0, movement = 1.4, highMovementShare = 0.8))

        assertEquals(ActivationLevel.LOW, afterWalking?.level)
    }

    @Test
    fun `windows without enough heart rate keep the previous estimate`() {
        engine.feed(window(heartRate = 66.0))

        val estimate = engine.feed(window(heartRate = 90.0, heartRateCount = 30))

        assertEquals(ActivationLevel.LOW, estimate?.level)
    }

    @Test
    fun `no estimate until a clean window arrives`() {
        assertNull(engine.feed(window(heartRate = 95.0, movement = 1.4, highMovementShare = 0.8)))
    }

    @Test
    fun `clean windows update the estimate once confirmed`() {
        engine.feed(window(heartRate = 66.0))
        engine.feed(window(heartRate = 85.0))

        assertEquals(ActivationLevel.HIGH, engine.feed(window(heartRate = 85.0))?.level)
    }

    private fun StateEngine.feed(window: FeatureWindow) =
        onWindow(window, baseline, DefaultCategoryProfiles.STUDY, 10.minutes)

    private fun window(
        heartRate: Double,
        movement: Double = 0.05,
        highMovementShare: Double = 0.0,
        heartRateCount: Int = 180,
    ) = FeatureWindow(
        start = Instant.EPOCH,
        end = Instant.EPOCH.plusSeconds(180),
        sampleCount = 180,
        heartRateCount = heartRateCount,
        meanHeartRate = heartRate,
        heartRateStdDev = 1.0,
        heartRateMeanAbsDiff = 1.0,
        meanMovement = movement,
        fidgetCount = 0,
        highMovementShare = highMovementShare,
    )
}
