package com.stateai.domain.summary

import com.stateai.domain.learning.ValidSessionRule
import com.stateai.domain.segment.Segment
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.DisplayState
import kotlin.time.Duration

/**
 * What the session summary shows, built from a stored segment. Past sessions only keep the three
 * classifier levels per minute, so "Agotado" (a live combination, decision 26) never appears here.
 *
 * @property minutes display state of each minute window, null when there was no usable estimate.
 * @property counted whether the session counts towards the activity's learning.
 */
data class SessionReport(
    val segment: Segment,
    val minutes: List<DisplayState?>,
    val timeByState: Map<DisplayState, Duration>,
    val noDataTime: Duration,
    val evaluablePauses: Int,
    val helpfulPauses: Int,
    val counted: Boolean,
) {
    val duration: Duration get() = segment.duration
    val planned: Duration get() = segment.planned

    companion object {
        fun of(segment: Segment, validRule: ValidSessionRule = ValidSessionRule()): SessionReport {
            val evaluable = segment.pauses.filter { it.isEvaluable }
            return SessionReport(
                segment = segment,
                minutes = segment.trace.levels().map { level -> level?.let(::displayStateOf) },
                timeByState = mapOf(
                    DisplayState.FOCUSED to segment.levelTime.low,
                    DisplayState.NORMAL to segment.levelTime.medium,
                    DisplayState.OVERLOADED to segment.levelTime.high,
                ),
                noDataTime = segment.levelTime.unknown,
                evaluablePauses = evaluable.size,
                helpfulPauses = evaluable.count { it.improved },
                counted = validRule.isValid(segment),
            )
        }

        private fun displayStateOf(level: ActivationLevel): DisplayState = when (level) {
            ActivationLevel.LOW -> DisplayState.FOCUSED
            ActivationLevel.MEDIUM -> DisplayState.NORMAL
            ActivationLevel.HIGH -> DisplayState.OVERLOADED
        }
    }
}
