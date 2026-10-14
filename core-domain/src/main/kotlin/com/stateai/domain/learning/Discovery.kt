package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityCategory

/** A third of the day, by the local hour a session started. */
enum class DayPart {
    MORNING,
    AFTERNOON,
    EVENING,
    ;

    companion object {
        private const val MORNING_FROM = 5
        private const val AFTERNOON_FROM = 12
        private const val EVENING_FROM = 18

        fun of(hour: Int): DayPart = when (hour) {
            in MORNING_FROM until AFTERNOON_FROM -> MORNING
            in AFTERNOON_FROM until EVENING_FROM -> AFTERNOON
            else -> EVENING
        }
    }
}

/** How much evidence stands behind a finding. It is never more than provisional evidence. */
enum class DiscoveryConfidence {
    PROVISIONAL,
    MODERATE,
}

/**
 * What the person's own ratings say about their sessions. It compares ratings, which are a personal
 * judgement, and never claims to measure performance or to know the cause of a difference.
 */
sealed interface Discovery {
    /** Too few rated sessions to say anything yet. */
    data class NotEnoughData(val ratedSessions: Int) : Discovery

    /** Enough rated sessions, but no moment of the day stands out. */
    data class NoClearDifference(val ratedSessions: Int) : Discovery

    /**
     * The sessions of [category] started in [part] were rated higher than the rest of that
     * category's sessions: [average] against [otherAverage], from [sessions] and [otherSessions].
     */
    data class BetterMoment(
        val category: ActivityCategory,
        val part: DayPart,
        val average: Double,
        val otherAverage: Double,
        val sessions: Int,
        val otherSessions: Int,
        val confidence: DiscoveryConfidence,
    ) : Discovery

    companion object {
        val EMPTY: Discovery = NotEnoughData(ratedSessions = 0)
    }
}
