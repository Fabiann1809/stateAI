package com.stateai.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_learning")
data class ActivityLearningEntity(
    @PrimaryKey val activityId: String,
    val validSessions: Int,
    val meanDurationSeconds: Long?,
    val meanMovement: Double?,
    val sensitivityAdjustment: Double,
)
