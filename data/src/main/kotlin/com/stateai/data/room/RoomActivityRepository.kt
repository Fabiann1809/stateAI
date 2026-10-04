package com.stateai.data.room

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityKey
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.ActivityStatus
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActivityRepository(private val dao: ActivityDao, private val clock: Clock) : ActivityRepository {
    override fun observeActive(): Flow<List<Activity>> =
        dao.observeActive(ACTIVE).map { entities -> entities.map { it.toDomain() } }

    override suspend fun findById(id: ActivityId): Activity? = dao.findById(id.value)?.toDomain()

    override suspend fun findByKey(key: ActivityKey): Activity? =
        dao.findByKey(key.category.name, key.name?.normalized)?.toDomain()

    override suspend fun countActive(): Int = dao.countActive(ACTIVE)

    override suspend fun add(activity: Activity) {
        dao.insert(activity.toEntity(createdAtMillis = clock.millis()))
    }

    override suspend fun markUsed(id: ActivityId, at: Instant) {
        dao.markUsed(id.value, at.toEpochMilli())
    }

    private companion object {
        val ACTIVE = ActivityStatus.ACTIVE.name
    }
}
