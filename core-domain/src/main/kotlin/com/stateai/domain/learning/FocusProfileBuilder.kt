package com.stateai.domain.learning

import com.stateai.domain.segment.Segment
import com.stateai.domain.state.ActivationLevel
import java.time.LocalDate
import java.time.ZoneId

/**
 * Builds the [FocusProfile] from segment traces. Window `i` of a segment ends `i + 1` minutes after
 * its start and is attributed to that local hour; windows without an estimate are ignored.
 */
class FocusProfileBuilder(private val zone: ZoneId) {
    private data class Observation(val slot: TimeSlot, val date: LocalDate, val focused: Boolean)

    fun build(segments: List<Segment>): FocusProfile {
        val observations = segments.flatMap(::observationsOf)
        if (observations.isEmpty()) return FocusProfile.EMPTY
        return FocusProfile(
            bySlot = observations.groupBy { it.slot }.mapValues { (_, slot) -> statsOf(slot) },
            byHour = observations.groupBy { it.slot.hour }.mapValues { (_, hour) -> statsOf(hour) },
            overallFocusShare = observations.count { it.focused }.toDouble() / observations.size,
        )
    }

    private fun observationsOf(segment: Segment): List<Observation> =
        segment.trace.levels().mapIndexedNotNull { index, level ->
            level ?: return@mapIndexedNotNull null
            val time = segment.start.plusSeconds((index + 1L) * SECONDS_PER_WINDOW).atZone(zone)
            Observation(TimeSlot(time.dayOfWeek, time.hour), time.toLocalDate(), level == ActivationLevel.LOW)
        }

    private fun statsOf(observations: List<Observation>) = SlotStats(
        focusShare = observations.count { it.focused }.toDouble() / observations.size,
        windows = observations.size,
        days = observations.map { it.date }.distinct().size,
    )

    private companion object {
        const val SECONDS_PER_WINDOW = 60L
    }
}
