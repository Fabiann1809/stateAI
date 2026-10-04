package com.stateai.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LearningDao {
    @Query("SELECT * FROM activity_learning WHERE activityId = :activityId")
    suspend fun load(activityId: String): ActivityLearningEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(learning: ActivityLearningEntity)
}
