package com.stateai.domain.baseline

import com.stateai.domain.sensing.SensorSample
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class BaselineCalibratorTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")
    private val calibrator = BaselineCalibrator()

    @Test
    fun `collects for two minutes and then produces the baseline`() {
        val progress = feed(seconds = 121) { 64.0 + it % 2 * 2 }

        val baseline = assertInstanceOf(CalibrationProgress.Done::class.java, progress).baseline
        assertEquals(65.0, baseline.restingHeartRate, 0.05)
        assertEquals(2.0, baseline.heartRateMeanAbsDiff, 1e-9)
    }

    @Test
    fun `reports progress before the time is up`() {
        val progress = feed(seconds = 61) { 65.0 }

        assertEquals(0.5f, (progress as CalibrationProgress.Collecting).fraction, 0.01f)
    }

    @Test
    fun `keeps collecting when heart rate was mostly missing`() {
        val progress = feed(seconds = 121) { if (it % 4 == 0) 65.0 else null }

        assertInstanceOf(CalibrationProgress.Collecting::class.java, progress)
    }

    @Test
    fun `ignores samples with movement`() {
        val progress = feed(seconds = 121, movement = { if (it < 60) 2.0 else 0.1 }) { 65.0 }

        assertInstanceOf(CalibrationProgress.Collecting::class.java, progress)
    }

    private fun feed(
        seconds: Int,
        movement: (Int) -> Double = { 0.1 },
        heartRate: (Int) -> Double?,
    ): CalibrationProgress = (0 until seconds).map { second ->
        calibrator.add(SensorSample(start.plusSeconds(second.toLong()), heartRate(second), movement(second)))
    }.last()
}
