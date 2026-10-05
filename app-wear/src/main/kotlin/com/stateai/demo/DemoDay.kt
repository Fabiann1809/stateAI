package com.stateai.demo

import com.stateai.domain.activity.Activity
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.PI
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * One simulated day of the [DemoRoutine]. Its [random] is seeded by the date, so the same day is
 * always generated the same way, whichever day the demo is loaded on.
 */
class DemoDay(private val zone: ZoneId, private val date: LocalDate, private val random: Random) {
    private val traces = DemoTrace(random)
    private val phase = random.nextDouble() * 2 * PI

    fun segments(activities: Map<DemoActivity, Activity>): List<Segment> =
        DemoRoutine.sessionsOn(date.dayOfWeek).mapIndexed { index, planned ->
            segment("demo-$date-$index", date, planned, activities.getValue(planned.activity), phase)
        }

    private fun segment(
        id: String,
        date: LocalDate,
        planned: PlannedDemoSession,
        activity: Activity,
        phase: Double,
    ): Segment {
        val startMinute = planned.startMinute + random.nextInt(-JITTER_MINUTES, JITTER_MINUTES + 1)
        val length = (planned.minutes + random.nextInt(-JITTER_MINUTES, JITTER_MINUTES + 1)).coerceAtLeast(MIN_MINUTES)
        val start = date.atStartOfDay(zone).toInstant().plusSeconds(startMinute * SECONDS_PER_MINUTE)
        val trace = traces.levels(startMinute, length, phase)
        val target = DefaultCategoryProfiles.of(activity.category).targetBlock
        val pauses = pausesIn(start, length)
        return Segment(
            id = SegmentId(id),
            activity = activity,
            start = start,
            end = start.plusSeconds(length * SECONDS_PER_MINUTE),
            planned = target,
            levelTime = durationsOf(trace),
            restlessTime = Duration.ZERO,
            pauseSuggestions = pauses.size,
            pauses = pauses,
            feedback = feedbackFor(trace),
            trace = trace,
            cueMinutes = listOfNotNull(0, target.inWholeMinutes.toInt().takeIf { it <= length }),
            calmHeartRate = CALM_HEART_RATE + random.nextDouble(-1.0, 1.0),
            cleanMovement = CLEAN_MOVEMENT,
        )
    }

    /** About half the sessions have a guided pause; most of them leave the person calmer. */
    private fun pausesIn(start: Instant, length: Int): List<PauseRecord> {
        if (random.nextDouble() > PAUSE_SHARE) return emptyList()
        val pauseStart = start.plusSeconds(length / 2L * SECONDS_PER_MINUTE)
        val before = if (random.nextBoolean()) ActivationLevel.HIGH else ActivationLevel.MEDIUM
        val after = if (random.nextDouble() < HELPFUL_PAUSE_SHARE) ActivationLevel.LOW else before
        return listOf(
            PauseRecord(
                start = pauseStart,
                end = pauseStart.plusSeconds(PAUSE_SECONDS),
                before = StateEstimate(before, restless = false),
                after = StateEstimate(after, restless = false),
            ),
        )
    }

    private fun durationsOf(trace: LevelTrace): LevelDurations =
        trace.levels().fold(LevelDurations()) { total, level -> total.plus(level, 1.minutes) }

    private fun feedbackFor(trace: LevelTrace): Feedback {
        val levels = trace.levels().filterNotNull()
        val focus = levels.count { it == ActivationLevel.LOW }.toDouble() / levels.size.coerceAtLeast(1)
        return when {
            focus >= GOOD_FOCUS -> Feedback.GOOD
            focus >= OKAY_FOCUS -> Feedback.OKAY
            else -> Feedback.BAD
        }
    }

    private companion object {
        const val JITTER_MINUTES = 10
        const val MIN_MINUTES = 15
        const val SECONDS_PER_MINUTE = 60L
        const val PAUSE_SECONDS = 120L
        const val PAUSE_SHARE = 0.5
        const val HELPFUL_PAUSE_SHARE = 0.7
        const val CALM_HEART_RATE = 65.0
        const val CLEAN_MOVEMENT = 0.05
        const val GOOD_FOCUS = 0.6
        const val OKAY_FOCUS = 0.4
    }
}
