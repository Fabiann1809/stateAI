package com.stateai.domain.haptics

import com.stateai.domain.session.SuggestionReason
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate

/** Window counts behind the state-driven haptic rules (one window per minute). */
data class HapticPolicyConfig(val sustainedWindows: Int = 3, val minFocusWindowsBeforePause: Int = 10)

/**
 * Decides which haptic cue, if any, a new estimate deserves (SPEC 6.2):
 * - focus (`LOW`) is never interrupted, except by sustained restlessness;
 * - `HIGH` sustained for [HapticPolicyConfig.sustainedWindows] windows → overload alert, once per episode;
 * - restlessness sustained as long → suggested pause, once per episode;
 * - leaving a long enough focus stretch → suggested pause.
 * The result still goes through the rate limiter before vibrating.
 */
class HapticPolicy(private val config: HapticPolicyConfig = HapticPolicyConfig()) {
    private var previousLevel: ActivationLevel? = null
    private var levelStreak = 0
    private var restlessStreak = 0
    private var overloadAlerted = false
    private var restlessAlerted = false

    /** The cue this estimate deserves, with the reason the screen shows next to the vibration. */
    fun onEstimate(estimate: StateEstimate): SessionCue? {
        val focusStreakBefore = if (previousLevel == ActivationLevel.LOW) levelStreak else 0
        updateStreaks(estimate)
        return when {
            sustainedOverload() -> {
                overloadAlerted = true
                SessionCue(HapticEvent.OVERLOAD_ALERT, SuggestionReason.OVERLOAD)
            }
            sustainedRestlessness() -> {
                restlessAlerted = true
                SessionCue(HapticEvent.PAUSE_SUGGESTED, SuggestionReason.RESTLESS)
            }
            leftFocus(
                estimate,
                focusStreakBefore,
            ) -> SessionCue(HapticEvent.PAUSE_SUGGESTED, SuggestionReason.LEFT_FOCUS)
            else -> null
        }
    }

    private fun updateStreaks(estimate: StateEstimate) {
        levelStreak = if (estimate.level == previousLevel) levelStreak + 1 else 1
        if (estimate.level != ActivationLevel.HIGH) overloadAlerted = false
        restlessStreak = if (estimate.restless) restlessStreak + 1 else 0
        if (!estimate.restless) restlessAlerted = false
        previousLevel = estimate.level
    }

    private fun sustainedOverload() =
        previousLevel == ActivationLevel.HIGH && levelStreak >= config.sustainedWindows && !overloadAlerted

    private fun sustainedRestlessness() = restlessStreak >= config.sustainedWindows && !restlessAlerted

    private fun leftFocus(estimate: StateEstimate, focusStreakBefore: Int) =
        estimate.level != ActivationLevel.LOW && focusStreakBefore >= config.minFocusWindowsBeforePause
}
