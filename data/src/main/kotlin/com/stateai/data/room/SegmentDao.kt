package com.stateai.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SegmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: SegmentEntity)

    @Insert
    suspend fun insertPauses(pauses: List<PauseEntity>)

    @Query("DELETE FROM pauses WHERE segmentId = :segmentId")
    suspend fun deletePauses(segmentId: String)

    @Transaction
    suspend fun save(segment: SegmentEntity, pauses: List<PauseEntity>) {
        insertSegment(segment)
        deletePauses(segment.id)
        insertPauses(pauses)
    }

    @Query("UPDATE segments SET feedback = :feedback WHERE id = :id")
    suspend fun setFeedback(id: String, feedback: String)

    @Transaction
    @Query("SELECT * FROM segments WHERE startMillis >= :fromMillis AND startMillis < :toMillis ORDER BY startMillis")
    fun observeBetween(fromMillis: Long, toMillis: Long): Flow<List<SegmentWithPauses>>

    @Transaction
    @Query("SELECT * FROM segments ORDER BY startMillis")
    suspend fun all(): List<SegmentWithPauses>
}
