package com.stateai.domain.activity

import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** Storage of the user's activities. Implementations live outside the domain. */
interface ActivityRepository {
    /** Active activities, most recently used first; never-used ones go last in creation order. */
    fun observeActive(): Flow<List<Activity>>

    suspend fun findById(id: ActivityId): Activity?

    suspend fun findByKey(key: ActivityKey): Activity?

    suspend fun countActive(): Int

    suspend fun add(activity: Activity)

    suspend fun markUsed(id: ActivityId, at: Instant)
}
