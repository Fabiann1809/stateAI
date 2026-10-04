package com.stateai.domain.testing

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityKey
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.ActivityStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Minimal in-memory repository for domain tests. */
class FakeActivityRepository(initial: List<Activity> = emptyList()) : ActivityRepository {
    private val activities = MutableStateFlow(initial)
    val lastUsed = mutableMapOf<ActivityId, Instant>()

    override fun observeActive(): Flow<List<Activity>> = activities

    override suspend fun findById(id: ActivityId): Activity? = activities.value.firstOrNull { it.id == id }

    override suspend fun findByKey(key: ActivityKey): Activity? = activities.value.firstOrNull { it.key == key }

    override suspend fun countActive(): Int = activities.value.count { it.status == ActivityStatus.ACTIVE }

    override suspend fun add(activity: Activity) {
        activities.value = activities.value + activity
    }

    override suspend fun markUsed(id: ActivityId, at: Instant) {
        lastUsed[id] = at
    }
}
