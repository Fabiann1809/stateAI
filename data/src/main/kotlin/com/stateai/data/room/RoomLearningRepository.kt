package com.stateai.data.room

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.learning.ActivityLearning
import com.stateai.domain.learning.LearningRepository
import kotlin.time.Duration.Companion.seconds

class RoomLearningRepository(private val dao: LearningDao) : LearningRepository {
    override suspend fun load(activityId: ActivityId): ActivityLearning? = dao.load(activityId.value)?.toDomain()

    override suspend fun save(learning: ActivityLearning) {
        dao.save(learning.toEntity())
    }
}

internal fun ActivityLearning.toEntity() = ActivityLearningEntity(
    activityId = activityId.value,
    validSessions = validSessions,
    meanDurationSeconds = meanDuration?.inWholeSeconds,
    meanMovement = meanMovement,
    sensitivityAdjustment = sensitivityAdjustment,
)

internal fun ActivityLearningEntity.toDomain() = ActivityLearning(
    activityId = ActivityId(activityId),
    validSessions = validSessions,
    meanDuration = meanDurationSeconds?.seconds,
    meanMovement = meanMovement,
    sensitivityAdjustment = sensitivityAdjustment,
)
