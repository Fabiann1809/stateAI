package com.stateai.domain.cycles

import com.stateai.domain.segment.Segment
import java.util.Random
import kotlin.math.abs

/** Outcome of cycle detection. Saying "no clear pattern" is an expected, honest answer. */
sealed interface CycleResult {
    data class Detected(val periodMinutes: Double, val pValue: Double) : CycleResult

    data class NoClearPattern(val reason: Reason) : CycleResult

    enum class Reason {
        NOT_ENOUGH_DATA,
        NOT_SIGNIFICANT,
        MATCHES_BLOCK_LENGTH,
        INCONSISTENT_HALVES,
    }
}

/** Search range, data requirements and significance of the cycle detection. */
data class CycleConfig(
    val minPeriodMinutes: Double = 40.0,
    val maxPeriodMinutes: Double = 150.0,
    val periodCount: Int = 200,
    val minObservations: Int = 300,
    val minSessions: Int = 6,
    val permutations: Int = 1000,
    val maxPValue: Double = 0.01,
    val blockMatchTolerance: Double = 0.1,
    val halvesTolerance: Double = 0.1,
    val seed: Long = 7L,
)

/**
 * Looks for a focus cycle (SPEC 7.4) with a periodogram and a permutation test. The null model
 * shifts every session to a random time, which keeps each session's own shape but breaks any rhythm
 * shared across sessions. Patterns tied to session boundaries (for example the end-of-block cue)
 * survive the shifts, so they are not reported as cycles. A detected period must also be found
 * independently in the first and second half of the history, which rules out aliases caused by a
 * regular daily schedule.
 */
class CycleDetector(
    private val config: CycleConfig = CycleConfig(),
    private val series: FocusSeriesBuilder = FocusSeriesBuilder(),
) {
    fun detect(segments: List<Segment>): CycleResult {
        val sessions = series.build(segments)
        if (sessions.size < config.minSessions || sessions.sumOf { it.size } < config.minObservations) {
            return CycleResult.NoClearPattern(CycleResult.Reason.NOT_ENOUGH_DATA)
        }
        val periodogram = Periodogram(sessions, periodGrid())
        val period = peakPeriod(periodogram)
        val pValue = pValue(periodogram, periodogram.powers().max(), sessions.size)
        return when {
            pValue > config.maxPValue -> CycleResult.NoClearPattern(CycleResult.Reason.NOT_SIGNIFICANT)
            !halvesAgree(sessions, period) -> CycleResult.NoClearPattern(CycleResult.Reason.INCONSISTENT_HALVES)
            matchesBlockLength(period, segments) -> CycleResult.NoClearPattern(CycleResult.Reason.MATCHES_BLOCK_LENGTH)
            else -> CycleResult.Detected(period, pValue)
        }
    }

    private fun halvesAgree(sessions: List<SessionSeries>, period: Double): Boolean {
        val ordered = sessions.sortedBy { it.minutes.first() }
        val halves = listOf(ordered.subList(0, ordered.size / 2), ordered.subList(ordered.size / 2, ordered.size))
        return halves.all { half ->
            abs(peakPeriod(Periodogram(half, periodGrid())) - period) <=
                period * config.halvesTolerance
        }
    }

    private fun peakPeriod(periodogram: Periodogram): Double {
        val powers = periodogram.powers()
        return periodogram.periodsMinutes[powers.indices.maxBy { powers[it] }]
    }

    /** Periods evenly spaced in frequency between the minimum and maximum period. */
    private fun periodGrid(): DoubleArray {
        val lowFrequency = 1 / config.maxPeriodMinutes
        val highFrequency = 1 / config.minPeriodMinutes
        val step = (highFrequency - lowFrequency) / (config.periodCount - 1)
        return DoubleArray(config.periodCount) { 1 / (lowFrequency + it * step) }
    }

    private fun pValue(periodogram: Periodogram, observedPeak: Double, sessionCount: Int): Double {
        val random = Random(config.seed)
        val exceed = (1..config.permutations).count {
            val shifts = DoubleArray(sessionCount) { random.nextDouble() * config.maxPeriodMinutes * SHIFT_SPAN }
            periodogram.powers(shifts).max() >= observedPeak
        }
        return (exceed + 1.0) / (config.permutations + 1.0)
    }

    private fun matchesBlockLength(period: Double, segments: List<Segment>): Boolean {
        val blocks = segments.map { it.planned.inWholeMinutes.toDouble() }.sorted()
        val typicalBlock = blocks[blocks.size / 2]
        return abs(period - typicalBlock) <= typicalBlock * config.blockMatchTolerance
    }

    private companion object {
        /** Shifts span several maximum periods so every phase is equally likely. */
        const val SHIFT_SPAN = 4
    }
}
