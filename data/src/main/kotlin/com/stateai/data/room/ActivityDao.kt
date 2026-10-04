package com.stateai.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    /** Active activities: most recently used first, never-used ones last in creation order. */
    @Query(
        "SELECT * FROM activities WHERE status = :active " +
            "ORDER BY lastUsedAtMillis IS NULL, lastUsedAtMillis DESC, createdAtMillis ASC",
    )
    fun observeActive(active: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun findById(id: String): ActivityEntity?

    @Query(
        "SELECT * FROM activities WHERE category = :category " +
            "AND ((:normalizedName IS NULL AND normalizedName IS NULL) OR normalizedName = :normalizedName) LIMIT 1",
    )
    suspend fun findByKey(category: String, normalizedName: String?): ActivityEntity?

    @Query("SELECT COUNT(*) FROM activities WHERE status = :active")
    suspend fun countActive(active: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(activity: ActivityEntity)

    @Query("UPDATE activities SET lastUsedAtMillis = :atMillis WHERE id = :id")
    suspend fun markUsed(id: String, atMillis: Long)
}
