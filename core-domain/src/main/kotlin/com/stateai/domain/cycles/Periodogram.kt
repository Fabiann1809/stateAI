package com.stateai.domain.cycles

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Schuster periodogram of unevenly sampled sessions, `P(w) = (C^2 + S^2) / (N * variance)` with
 * `C = sum(y * cos(w t))` and `S = sum(y * sin(w t))` over the mean-centered series.
 *
 * Per-session sums are computed once, so the power after shifting every session in time by `d`
 * minutes is cheap: `C' = sum(cos(w d) C_s - sin(w d) S_s)` and `S' = sum(sin(w d) C_s + cos(w d) S_s)`.
 * That makes a permutation test with thousands of shifts affordable on a watch.
 */
class Periodogram(sessions: List<SessionSeries>, val periodsMinutes: DoubleArray) {
    private val frequencies = DoubleArray(periodsMinutes.size) { 2 * PI / periodsMinutes[it] }
    private val sessionCount = sessions.size
    private val cosSums = Array(sessionCount) { DoubleArray(frequencies.size) }
    private val sinSums = Array(sessionCount) { DoubleArray(frequencies.size) }
    private val normalization: Double

    init {
        val all = sessions.flatMap { it.values.asList() }
        val mean = all.average()
        val variance = all.sumOf { (it - mean) * (it - mean) } / all.size
        normalization = all.size * variance
        sessions.forEachIndexed { s, series ->
            for (f in frequencies.indices) {
                var c = 0.0
                var sn = 0.0
                for (i in 0 until series.size) {
                    val centered = series.values[i] - mean
                    c += centered * cos(frequencies[f] * series.minutes[i])
                    sn += centered * sin(frequencies[f] * series.minutes[i])
                }
                cosSums[s][f] = c
                sinSums[s][f] = sn
            }
        }
    }

    /** Power at every period after shifting session `s` by `shiftsMinutes[s]` minutes. */
    fun powers(shiftsMinutes: DoubleArray = DoubleArray(sessionCount)): DoubleArray =
        DoubleArray(frequencies.size) { f ->
            var c = 0.0
            var sn = 0.0
            for (s in 0 until sessionCount) {
                val angle = frequencies[f] * shiftsMinutes[s]
                c += cos(angle) * cosSums[s][f] - sin(angle) * sinSums[s][f]
                sn += sin(angle) * cosSums[s][f] + cos(angle) * sinSums[s][f]
            }
            if (normalization > 0) (c * c + sn * sn) / normalization else 0.0
        }
}
