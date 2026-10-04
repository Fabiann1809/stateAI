package com.stateai.domain.cycles

import com.stateai.domain.testing.testSegment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Statistical behavior over many synthetic users (three weeks, four sessions a day with gaps).
 * Seeds are fixed for reproducibility; the assertions are rates, not hand-picked cases.
 */
class CycleDetectorTest {
    private val detector = CycleDetector()

    @ParameterizedTest
    @ValueSource(doubles = [60.0, 90.0, 110.0])
    fun `recovers known cycles in most users and never reports a wrong period`(period: Double) {
        val results = SEEDS.map { detector.detect(SyntheticFocusUser(it, DAYS).withCycle(period)) }
        val detected = results.filterIsInstance<CycleResult.Detected>()

        assertTrue(detected.size >= MIN_DETECTIONS, "detected ${detected.size} of ${SEEDS.count()}: $results")
        detected.forEach { assertEquals(period, it.periodMinutes, period * TOLERANCE, "$results") }
    }

    @Test
    fun `users without a cycle get no clear pattern`() {
        val results = NULL_SEEDS.map { detector.detect(SyntheticFocusUser(it, DAYS).withoutCycle()) }

        assertTrue(results.none { it is CycleResult.Detected }, "$results")
    }

    @Test
    fun `end-of-block cues do not create a false cycle`() {
        val results = (1L..5L).map { detector.detect(SyntheticFocusUser(it, DAYS).withBlockEndDrop()) }

        assertTrue(results.none { it is CycleResult.Detected }, "$results")
    }

    @Test
    fun `too little data gives no clear pattern`() {
        val result = detector.detect(List(3) { testSegment(id = "$it") })

        assertEquals(CycleResult.NoClearPattern(CycleResult.Reason.NOT_ENOUGH_DATA), result)
    }

    @Test
    fun `a period equal to the planned block is not reported`() {
        val sessions = SyntheticFocusUser(seed = 4, DAYS).withCycle(60.0).map { it.copy(planned = it.planned * 1.5) }

        assertEquals(CycleResult.NoClearPattern(CycleResult.Reason.MATCHES_BLOCK_LENGTH), detector.detect(sessions))
    }

    private companion object {
        const val DAYS = 21
        const val TOLERANCE = 0.1
        const val MIN_DETECTIONS = 6
        val SEEDS = 1L..10L
        val NULL_SEEDS = 1L..20L
    }
}
