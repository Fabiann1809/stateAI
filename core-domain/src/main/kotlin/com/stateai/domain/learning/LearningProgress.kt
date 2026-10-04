package com.stateai.domain.learning

import com.stateai.domain.activity.ActivityId

/**
 * Whether stateAI is still learning about some activities (SPEC 6.7): true while any of them has
 * fewer than [threshold] valid sessions. Shown as a discreet hint without numbers, so the app does
 * not suggest a precision it does not have yet.
 */
class LearningProgress(private val repository: LearningRepository, private val threshold: Int = DEFAULT_THRESHOLD) {
    suspend fun isLearning(activities: Collection<ActivityId>): Boolean =
        activities.any { (repository.load(it)?.validSessions ?: 0) < threshold }

    companion object {
        const val DEFAULT_THRESHOLD = 5
    }
}
