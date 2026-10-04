package com.stateai.domain.segment

import com.stateai.domain.activity.Activity
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

@JvmInline
value class SegmentId(val value: String)

/** One-tap answer to "how did you feel?" when a segment ends. */
enum class Feedback {
    GOOD,
    OKAY,
    BAD,
}

/** A guided pause and the estimates right before and right after it. */
data class PauseRecord(val start: Instant, val end: Instant, val before: StateEstimate?, val after: StateEstimate?) {
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
) {
    val duration: Duration get() = java.time.Duration.between(start, end).toKotlinDuration()
}

/** All segments of one local day. */
data class DayRecord(val date: LocalDate, val segments: List<Segment>)
