package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.segment.Segment
import java.time.LocalDate
import java.time.ZoneId

/**
 * Thresholds of the discovery. They are deliberately strict, so a finding needs several sessions on
 * several days before it is shown.
 *
 * @property minSessions rated sessions needed in a moment of the day, and in the rest of the category.
 * @property minDays different days those sessions must come from.
 * @property minDifference how much higher (on the 1-to-5 scale) the average must be.
 * @property moderateSessions sessions on each side needed for a moderate confidence.
 * @property moderateDifference difference needed for a moderate confidence.
 */
data class DiscoveryConfig(
    val minSessions: Int = 3,
    val minDays: Int = 2,
    val minDifference: Double = 0.5,
    val moderateSessions: Int = 6,
    val moderateDifference: Double = 1.0,
)

/**
 * Looks for a moment of the day at which the person rates their sessions higher, within the same kind
 * of activity (studying is not compared with meetings). Only rated sessions count; the heart rate
 * estimate is not used, so this is the person's experience and not the watch's measurement.
 */
class DiscoveryFinder(private val zone: ZoneId, private val config: DiscoveryConfig = DiscoveryConfig()) {
    private data class Rated(val category: ActivityCategory, val part: DayPart, val rating: Int, val date: LocalDate)

    fun find(segments: List<Segment>): Discovery {
        val rated = segments.mapNotNull(::ratedOf)
        if (rated.size < config.minSessions) return Discovery.NotEnoughData(rated.size)
        val candidates = rated.groupBy { it.category }.values.flatMap(::candidates)
        val best = candidates.maxByOrNull { it.average - it.otherAverage }
        return best ?: if (isEnoughToCompare(rated)) {
            Discovery.NoClearDifference(rated.size)
        } else {
            Discovery.NotEnoughData(rated.size)
        }
    }

    private fun ratedOf(segment: Segment): Rated? {
        val feedback = segment.feedback ?: return null
        val start = segment.start.atZone(zone)
        return Rated(segment.activity.category, DayPart.of(start.hour), feedback.rating, start.toLocalDate())
    }

    /** One candidate per moment of the day that has enough sessions against the rest of its category. */
    private fun candidates(category: List<Rated>): List<Discovery.BetterMoment> = DayPart.entries.mapNotNull { part ->
        val (inPart, rest) = category.partition { it.part == part }
        if (!hasEvidence(inPart) || !hasEvidence(rest)) return@mapNotNull null
        val average = inPart.map { it.rating }.average()
        val otherAverage = rest.map { it.rating }.average()
        if (average - otherAverage < config.minDifference) return@mapNotNull null
        Discovery.BetterMoment(
            category = category.first().category,
            part = part,
            average = average,
            otherAverage = otherAverage,
            sessions = inPart.size,
            otherSessions = rest.size,
            confidence = confidenceOf(inPart.size, rest.size, average - otherAverage),
        )
    }

    private fun hasEvidence(sessions: List<Rated>) =
        sessions.size >= config.minSessions && sessions.map { it.date }.distinct().size >= config.minDays

    private fun confidenceOf(sessions: Int, otherSessions: Int, difference: Double) =
        if (minOf(sessions, otherSessions) >= config.moderateSessions && difference >= config.moderateDifference) {
            DiscoveryConfidence.MODERATE
        } else {
            DiscoveryConfidence.PROVISIONAL
        }

    /** There is enough to compare when some category has rated sessions in two or more moments of the day. */
    private fun isEnoughToCompare(rated: List<Rated>) = rated.groupBy { it.category }.values.any { category ->
        category.groupBy { it.part }.values.count(::hasEvidence) >= 2
    }
}
