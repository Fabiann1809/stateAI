package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId
import kotlin.time.Duration

/**
 * What one activity has learned from its own valid sessions.
 *
 * @property validSessions `n` in the category/activity blending (SPEC 6.7).
 * @property meanDuration running mean duration of its valid sessions: its natural block length.
 * @property meanMovement running mean of the clean-window movement (m/s²).
 * @property sensitivityAdjustment relative widening (+) or narrowing (-) of the heart rate thresholds,
 *   learned slowly from feedback.
 */
data class ActivityLearning(
    val activityId: ActivityId,
    val validSessions: Int = 0,
    val meanDuration: Duration? = null,
    val meanMovement: Double? = null,
    val sensitivityAdjustment: Double = 0.0,
)

/** Storage of per-activity learning. */
interface LearningRepository {
    suspend fun load(activityId: ActivityId): ActivityLearning?

    suspend fun save(learning: ActivityLearning)
}
