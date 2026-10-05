package com.stateai.domain.cycles

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import java.io.File
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Cycle detection report (T-8.4): the app's real detector on the latent-state synthetic users of
 * `ml-python`, fed two ways: the trace the watch would record (signal, 3-minute windows, rules,
 * smoothing) and the ideal trace (the true latent level, as if classification were perfect). The
 * traces come from `python -m stateai_ml.cycle_traces`. Prints the table of `docs/cycles.md`.
 */
class CycleDetectionReportTest {
    private val detector = CycleDetector()

    @Test
    fun `report of real versus detected cycle length`() {
        val sessions = sessions().groupBy { it.user }
        val results = users().flatMap { user ->
            DAYS.map { days ->
                val recent = sessions.getValue(user.name).firstDays(days)
                Row(user, days, detector.detect(recent.map { it.watch }), detector.detect(recent.map { it.ideal }))
            }
        }
        println("| User | Real rhythm | Days | Watch trace (rules) | Ideal trace (true state) |")
        println("|---|---|---|---|---|")
        results.forEach { println(it.markdown()) }

        // Guards of what holds today (docs/cycles.md): no cycle is ever invented for users without one,
        // and what the watch pipeline detects is close to the real period. The ideal trace once reports a
        // wrong period; that is documented, not asserted.
        val withoutCycle = results.filter { it.user.period == null }
        assertTrue(withoutCycle.none { it.watch is CycleResult.Detected || it.ideal is CycleResult.Detected })
        assertTrue(results.all { it.isWithinTolerance(it.watch) })
    }

    private class TraceUser(val name: String, val period: Double?, val drift: Double) {
        val rhythm: String =
            period?.let { if (drift > 0) "${it.toInt()} +/- ${drift.toInt()} min" else "${it.toInt()} min" }
                ?: "none"
    }

    private class Session(val user: String, val watch: Segment, val ideal: Segment)

    private class Row(val user: TraceUser, val days: Int, val watch: CycleResult, val ideal: CycleResult) {
        fun markdown() = "| ${user.name} | ${user.rhythm} | $days | ${cell(watch)} | ${cell(ideal)} |"

        /** A detected period is never far from the real one (the irregular user drifts, so it gets the drift). */
        fun isWithinTolerance(result: CycleResult): Boolean {
            val period = user.period
            val detected = (result as? CycleResult.Detected)?.periodMinutes
            return period == null || detected == null || abs(detected - period) <= period * TOLERANCE + user.drift
        }

        private fun cell(result: CycleResult): String = when (result) {
            is CycleResult.Detected -> {
                val error = user.period?.let {
                    " (%+.1f %%)".format((result.periodMinutes - it) / it * PERCENT)
                }.orEmpty()
                "%.1f min".format(result.periodMinutes) + error + ", p = %.3f".format(result.pValue)
            }
            is CycleResult.NoClearPattern -> "no clear pattern (${result.reason.name.lowercase().replace('_', ' ')})"
        }
    }

    private fun List<Session>.firstDays(days: Int): List<Session> {
        val first = minOf { it.watch.start }
        return filter { Duration.between(first, it.watch.start).toDays() < days }
    }

    private fun users(): List<TraceUser> = rows("users.csv").map { cells ->
        TraceUser(cells[0], cells[2].takeIf { it.isNotEmpty() }?.toDouble(), cells[3].toDouble())
    }

    private fun sessions(): List<Session> = rows("traces.csv").mapIndexed { index, cells ->
        Session(cells[0], segment("w$index", cells, cells[6]), segment("i$index", cells, cells[7]))
    }

    private fun segment(id: String, cells: List<String>, trace: String): Segment {
        val start = OffsetDateTime.parse(cells[1]).toInstant()
        return Segment(
            id = SegmentId(id),
            activity = Activity(ActivityId(cells[4]), ActivityCategory.valueOf(cells[4]), name = null),
            start = start,
            end = start.plusSeconds(cells[2].toLong() * SECONDS_PER_MINUTE),
            planned = cells[3].toInt().minutes,
            levelTime = LevelDurations(),
            restlessTime = kotlin.time.Duration.ZERO,
            pauseSuggestions = 0,
            pauses = emptyList(),
            trace = LevelTrace(trace),
            cueMinutes = cells[5].split(';').filter { it.isNotEmpty() }.map(String::toInt),
        )
    }

    private fun rows(file: String): List<List<String>> =
        File(DIRECTORY, file).readLines().drop(1).filter { it.isNotBlank() }.map { it.split(',') }

    private companion object {
        const val DIRECTORY = "../shared/cycles"
        val DAYS = listOf(21, 14)
        const val PERCENT = 100
        const val TOLERANCE = 0.1
        const val SECONDS_PER_MINUTE = 60L
    }
}
