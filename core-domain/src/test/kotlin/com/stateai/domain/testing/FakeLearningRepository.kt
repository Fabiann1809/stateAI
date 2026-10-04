package com.stateai.domain.testing

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.learning.ActivityLearning
import com.stateai.domain.learning.LearningRepository

/** In-memory learning storage for domain tests. */
class FakeLearningRepository : LearningRepository {
    val stored = mutableMapOf<ActivityId, ActivityLearning>()

    override suspend fun load(activityId: ActivityId): ActivityLearning? = stored[activityId]

    override suspend fun save(learning: ActivityLearning) {
        stored[learning.activityId] = learning
    }
}
