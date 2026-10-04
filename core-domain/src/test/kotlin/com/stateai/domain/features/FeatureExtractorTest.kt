package com.stateai.domain.features

import com.stateai.domain.sensing.SensorSample
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FeatureExtractorTest {
    private val extractor = FeatureExtractor()
    private val start = Instant.parse("2026-10-04T09:00:00Z")

    @Test
    fun `computes heart rate statistics`() {
        val window = extractor.extract(samples(heartRates = listOf(60.0, 62.0, 64.0, 66.0)))!!

        assertEquals(63.0, window.meanHeartRate!!, 1e-9)
        assertEquals(2.2360679775, window.heartRateStdDev, 1e-9)
        assertEquals(2.0, window.heartRateMeanAbsDiff, 1e-9)
    }

    @Test
    fun `successive differences skip pairs with a missing heart rate`() {
        val window = extractor.extract(samples(heartRates = listOf(60.0, null, 64.0, 65.0)))!!

        assertEquals(1.0, window.heartRateMeanAbsDiff, 1e-9)
        assertEquals(3, window.heartRateCount)
    }

    @Test
    fun `no heart rate gives no mean`() {
        val window = extractor.extract(samples(heartRates = listOf(null, null)))!!

        assertNull(window.meanHeartRate)
        assertEquals(0.0, window.heartRateStdDev)
    }

    @Test
    fun `a burst of high movement counts as one fidget`() {
        val movements = listOf(0.1, 2.0, 2.4, 0.1, 0.1, 1.9, 0.2)
        val window = extractor.extract(samples(movements = movements))!!

        assertEquals(2, window.fidgetCount)
        assertEquals(0.9714285714, window.meanMovement, 1e-9)
    }

    @Test
    fun `high movement share counts sustained activity`() {
        val window = extractor.extract(samples(movements = listOf(1.2, 1.3, 0.1, 0.1)))!!

        assertEquals(0.5, window.highMovementShare, 1e-9)
    }

    @Test
    fun `calm window with heart rate is clean`() {
        val window = extractor.extract(samples(heartRates = List(10) { 65.0 }))!!

        assertTrue(extractor.isClean(window))
    }

    @Test
    fun `sustained movement or missing heart rate make a window unclean`() {
        val walking = extractor.extract(samples(movements = List(10) { 1.5 }))!!
        val noHeartRate = extractor.extract(samples(heartRates = List(10) { null }))!!

        assertFalse(extractor.isClean(walking))
        assertFalse(extractor.isClean(noHeartRate))
    }

    @Test
    fun `empty window has no features`() {
        assertNull(extractor.extract(emptyList()))
    }

    private fun samples(heartRates: List<Double?>? = null, movements: List<Double>? = null): List<SensorSample> {
        val size = heartRates?.size ?: movements!!.size
        return List(size) { index ->
            SensorSample(
                timestamp = start.plusSeconds(index.toLong()),
                heartRateBpm = heartRates?.get(index) ?: if (heartRates == null) 65.0 else null,
                movement = movements?.get(index) ?: 0.1,
            )
        }
    }
}
