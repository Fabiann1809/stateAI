package com.stateai.domain.cycles

import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.Segment
import com.stateai.domain.testing.testSegment
import java.time.Instant
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.time.Duration.Companion.minutes

/**
 * Test users with a known focus rhythm. Each day has four sessions at jittered times with gaps
 * between them; the rhythm keeps its phase within a day and gets a random phase every day.
 */
class SyntheticFocusUser(private val seed: Long, private val days: Int = 14) {
    private val sessionStartsMinutes = listOf(510, 660, 870, 1020)

    /** Focus probability follows `0.5 + amplitude * cos(2 pi t / period + dayPhase)`. */
    fun withCycle(periodMinutes: Double, amplitude: Double = 0.35): List<Segment> = sessions { random ->
        val phase = random.nextDouble() * 2 * PI
        return@sessions { minuteOfDay: Int -> 0.5 + amplitude * cos(2 * PI * minuteOfDay / periodMinutes + phase) }
    }

    /** No rhythm: focus comes in stretches of random length (sticky Markov chain). */
    fun withoutCycle(stickiness: Double = 0.9, focusRate: Double = 0.6): List<Segment> {
        val random = Random(seed)
        return buildSessions(random) { length, _ ->
            var focused = random.nextDouble() < focusRate
            List(length) {
                if (random.nextDouble() > stickiness) focused = random.nextDouble() < focusRate
                focused
            }
        }
    }

    /** No rhythm, but focus drops right after the end-of-block cue at [blockMinutes] in every session. */
    fun withBlockEndDrop(blockMinutes: Int = 40): List<Segment> {
        val random = Random(seed)
        return buildSessions(random, cueAt = blockMinutes) { length, _ ->
            List(length) { minute ->
                if (minute <
                    blockMinutes
                ) {
                    random.nextDouble() < 0.85
                } else {
                    random.nextDouble() < 0.2
                }
            }
        }
    }

    /** [probabilityForDay] is called once per day and returns the focus probability by minute of day. */
    private fun sessions(probabilityForDay: (Random) -> (Int) -> Double): List<Segment> {
        val random = Random(seed)
        var currentDay = -1
        var probability: (Int) -> Double = { 0.5 }
        return buildSessions(random) { length, (day, startMinute) ->
            if (day != currentDay) {
                currentDay = day
                probability = probabilityForDay(random)
            }
            List(length) { minute -> random.nextDouble() < probability(startMinute + minute + 1) }
        }
    }

    private fun buildSessions(
        random: Random,
        cueAt: Int? = null,
        focusOf: (Int, Pair<Int, Int>) -> List<Boolean>,
    ): List<Segment> = (0 until days).flatMap { day ->
        sessionStartsMinutes.map { base ->
            val start = base + random.nextInt(JITTER * 2) - JITTER
            val length = MIN_LENGTH + random.nextInt(MAX_LENGTH - MIN_LENGTH)
            val focus = focusOf(length, day to start)
            val trace = LevelTrace(focus.joinToString("") { if (it) "L" else "M" })
            testSegment(
                id = "$seed-$day-$base",
                start = ORIGIN.plusSeconds(day * SECONDS_PER_DAY + start * 60L),
                duration = length.minutes,
                trace = trace,
            ).let { segment -> cueAt?.let { segment.copy(cueMinutes = listOf(0, it)) } ?: segment }
        }
    }

    private companion object {
        val ORIGIN: Instant = Instant.parse("2026-09-01T00:00:00Z")
        const val SECONDS_PER_DAY = 86_400L
        const val JITTER = 20
        const val MIN_LENGTH = 50
        const val MAX_LENGTH = 80
    }
}
