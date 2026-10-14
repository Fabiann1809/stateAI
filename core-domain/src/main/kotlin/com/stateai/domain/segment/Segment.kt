package com.stateai.domain.segment

import com.stateai.domain.activity.Activity
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

@JvmInline
value class SegmentId(val value: String)

/**
 * The user's own 1-to-5 rating of how the session went, asked when a segment ends. It is a personal
 * judgement, kept apart from the activation estimated by the watch. The names are stored, so the
 * original `GOOD`, `OKAY` and `BAD` keep their meaning.
 */
enum class Feedback {
    VERY_BAD,
    BAD,
    OKAY,
    GOOD,
    VERY_GOOD,
    ;

    /** From 1 (very bad) to 5 (very good). */
    val rating: Int get() = ordinal + 1

    /** Whether the user felt the session went badly (1 or 2). */
    val isNegative: Boolean get() = rating <= NEGATIVE_MAX

    /** Whether the user felt the session went well (4 or 5). */
    val isPositive: Boolean get() = rating >= POSITIVE_MIN

    companion object {
        private const val NEGATIVE_MAX = 2
        private const val POSITIVE_MIN = 4

        fun fromRating(rating: Int): Feedback? = entries.firstOrNull { it.rating == rating }
    }
}

/** A guided pause and the estimates right before and right after it. */
data class PauseRecord(val start: Instant, val end: Instant, val before: StateEstimate?, val after: StateEstimate?) {
    /** Whether there are estimates on both sides, so the effect of the pause can be judged. */
    val isEvaluable: Boolean get() = before != null && after != null

    /** The pause helped when activation went down or restlessness went away. */
    val improved: Boolean
        get() {
            if (before == null || after == null) return false
            return after.level < before.level || (before.restless && !after.restless)
        }
}

/** One continuous activity within the day. Only summaries are kept, never the raw signal. */
data class Segment(
    val id: SegmentId,
    val activity: Activity,
    val start: Instant,
    val end: Instant,
    val planned: Duration,
    val levelTime: LevelDurations,
    val restlessTime: Duration,
    val pauseSuggestions: Int,
    val pauses: List<PauseRecord>,
    val feedback: Feedback? = null,
    val trace: LevelTrace = LevelTrace(),
    /** Minutes from the start at which the app played a time cue (block start or end). */
    val cueMinutes: List<Int> = emptyList(),
    /** Mean heart rate of the calm windows (`LOW`, not restless), used to refine the baseline. */
    val calmHeartRate: Double? = null,
    /** Mean movement of the clean windows, used to learn the activity's normal movement. */
    val cleanMovement: Double? = null,
) {
    val duration: Duration get() = java.time.Duration.between(start, end).toKotlinDuration()
}

/** All segments of one local day. */
data class DayRecord(val date: LocalDate, val segments: List<Segment>)
