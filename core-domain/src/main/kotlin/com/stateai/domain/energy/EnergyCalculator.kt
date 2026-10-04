package com.stateai.domain.energy

import com.stateai.domain.learning.FocusProfile
import com.stateai.domain.segment.Segment
import com.stateai.domain.state.ActivationLevel
import java.time.ZoneId

/**
 * Computes today's estimated energy from the day's segments (SPEC 6.5): every minute of session
 * consumes at the rate of its estimated level, scaled by how good that hour usually is for the
 * person; restless time costs extra; guided pauses that improved the state recover energy.
 */
class EnergyCalculator(private val zone: ZoneId, private val config: EnergyConfig = EnergyConfig()) {
    fun compute(segments: List<Segment>, profile: FocusProfile, dayStart: Double = config.dayStart): EnergyBudget {
        val consumed = segments.sumOf { consumption(it, profile) }
        val recovered = segments.sumOf { segment -> segment.pauses.count { it.improved } * config.usefulPauseRecovery }
        val level = (dayStart - consumed + recovered).coerceIn(0.0, dayStart)
        return EnergyBudget(level, consumed, recovered, isLow = level < config.lowThreshold)
    }

    private fun consumption(segment: Segment, profile: FocusProfile): Double {
        val levels = segment.trace.levels()
        val windowCost = levels.withIndex().sumOf { (index, level) ->
            val hour = segment.start.plusSeconds((index + 1L) * SECONDS_PER_MINUTE).atZone(zone).hour
            rateOf(level) * hourFactor(profile, hour)
        }
        val minutesWithoutWindow = (segment.duration.inWholeMinutes - levels.size).coerceAtLeast(0)
        val restlessCost = segment.restlessTime.inWholeMinutes * config.restlessExtraRate
        return windowCost + minutesWithoutWindow * config.unknownRate + restlessCost
    }

    private fun rateOf(level: ActivationLevel?): Double = when (level) {
        ActivationLevel.LOW -> config.focusRate
        ActivationLevel.MEDIUM -> config.normalRate
        ActivationLevel.HIGH -> config.overloadRate
        null -> config.unknownRate
    }

    private fun hourFactor(profile: FocusProfile, hour: Int): Double {
        val share = profile.byHour[hour]?.focusShare ?: return 1.0
        return when {
            share >= profile.overallFocusShare + config.hourMargin -> config.goodHourFactor
            share <= profile.overallFocusShare - config.hourMargin -> config.badHourFactor
            else -> 1.0
        }
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60L
    }
}
