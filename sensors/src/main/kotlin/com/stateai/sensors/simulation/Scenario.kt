package com.stateai.sensors.simulation

import kotlin.time.Duration

/** A scripted sequence of phases. The simulator loops it when it reaches the end. */
data class Scenario(val id: ScenarioId, val phases: List<SignalPhase>) {
    init {
        require(phases.isNotEmpty()) { "A scenario needs at least one phase" }
    }

    val totalDuration: Duration get() = phases.fold(Duration.ZERO) { total, phase -> total + phase.duration }

    /** The phase active at [second] (looping) and how far into that phase it is, from 0 to 1. */
    fun phaseAt(second: Long): Pair<SignalPhase, Double> {
        var remaining = second % totalDuration.inWholeSeconds
        for (phase in phases) {
            val length = phase.duration.inWholeSeconds
            if (remaining < length) return phase to remaining.toDouble() / length
            remaining -= length
        }
        error("Unreachable: second is always inside the looped scenario")
    }
}

/** The scripted scenarios available in the simulator. */
enum class ScenarioId {
    DEEP_FOCUS,
    OVERLOAD,
    FATIGUE,
    MIXED,
}
