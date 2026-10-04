package com.stateai.domain.learning

import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.Segment

/** Step and bounds of the feedback-driven sensitivity adjustment. */
data class FeedbackSensitivityConfig(
    val step: Double = 0.02,
    val limit: Double = 0.2,
    val mostlyFocusedShare: Double = 0.6,
    val mostlyLoadedShare: Double = 0.4,
)

/**
 * Slowly adjusts an activity's global sensitivity from the one-tap feedback (SPEC 7.6). Feedback
 * labels a whole segment, not individual windows, so it only nudges the thresholds:
 * - "bad" after a segment estimated as mostly focused means load was under-detected: more sensitive;
 * - "good" after a segment estimated as mostly loaded means load was over-detected: less sensitive;
 * - anything else leaves the sensitivity as it is.
 */
class FeedbackSensitivityLearner(
    private val repository: LearningRepository,
    private val config: FeedbackSensitivityConfig = FeedbackSensitivityConfig(),
) {
    suspend fun learn(segment: Segment, feedback: Feedback) {
        val change = changeFor(segment, feedback)
        if (change == 0.0) return
        val id = segment.activity.id
        val current = repository.load(id) ?: ActivityLearning(id)
        val adjusted = (current.sensitivityAdjustment + change).coerceIn(-config.limit, config.limit)
        repository.save(current.copy(sensitivityAdjustment = adjusted))
    }

    private fun changeFor(segment: Segment, feedback: Feedback): Double {
        val known = segment.levelTime.known
        if (!known.isPositive()) return 0.0
        val focusShare = segment.levelTime.low / known
        val loadShare = minOf(1.0, (segment.levelTime.high + segment.restlessTime) / known)
        return when {
            feedback == Feedback.BAD && focusShare >= config.mostlyFocusedShare -> -config.step
            feedback == Feedback.GOOD && loadShare >= config.mostlyLoadedShare -> config.step
            else -> 0.0
        }
    }
}
