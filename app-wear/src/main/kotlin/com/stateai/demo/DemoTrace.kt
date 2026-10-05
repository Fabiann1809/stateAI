package com.stateai.demo

import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.state.ActivationLevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

/**
 * Per-minute levels of a demo session. Focus is more likely in the morning and follows a gentle
 * rhythm of [RHYTHM_MINUTES] (with a new phase every day); the rest is mostly normal, some overload.
 * The first window has no estimate yet, like on the watch.
 */
class DemoTrace(private val random: Random) {
    fun levels(startMinuteOfDay: Int, minutes: Int, dayPhase: Double): LevelTrace {
        var trace = LevelTrace().plus(null)
        for (minute in 1 until minutes) {
            trace = trace.plus(levelAt(startMinuteOfDay + minute, dayPhase))
        }
        return trace
    }

    private fun levelAt(minuteOfDay: Int, dayPhase: Double): ActivationLevel {
        val morning = if (minuteOfDay < NOON) MORNING_BONUS else 0.0
        val rhythm = RHYTHM_AMPLITUDE * cos(2 * PI * minuteOfDay / RHYTHM_MINUTES + dayPhase)
        return when {
            random.nextDouble() < BASE_FOCUS + morning + rhythm -> ActivationLevel.LOW
            random.nextDouble() < OVERLOAD_SHARE -> ActivationLevel.HIGH
            else -> ActivationLevel.MEDIUM
        }
    }

    private companion object {
        const val NOON = 12 * 60
        const val BASE_FOCUS = 0.45
        const val MORNING_BONUS = 0.15
        const val RHYTHM_AMPLITUDE = 0.3
        const val RHYTHM_MINUTES = 90.0
        const val OVERLOAD_SHARE = 0.25
    }
}
