package com.stateai.domain.energy

import com.stateai.domain.learning.ObserveFocusProfile
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Today's estimated energy, including the session in progress, recomputed on every tick.
 * [clock] must be in the device's time zone so "today" matches the person's day.
 */
class ObserveEnergy(
    private val segments: SegmentRepository,
    private val observeFocusProfile: ObserveFocusProfile,
    private val recorder: SegmentRecorder,
    private val clock: Clock,
    private val calculator: EnergyCalculator = EnergyCalculator(clock.zone),
) {
    operator fun invoke(ticks: Flow<Instant>): Flow<EnergyBudget> {
        val today = LocalDate.now(clock)
        val from = today.atStartOfDay(clock.zone).toInstant()
        val to = today.plusDays(1).atStartOfDay(clock.zone).toInstant()
        return combine(segments.observeBetween(from, to), observeFocusProfile(), ticks) { day, profile, now ->
            calculator.compute(day + listOfNotNull(recorder.snapshot(now)), profile)
        }
    }
}
