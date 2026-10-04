package com.stateai.data.memory

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityKey
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.ActivityStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Volatile repository used until persistent storage exists; data is lost when the process dies. */
class InMemoryActivityRepository : ActivityRepository {
    private data class Entry(val activity: Activity, val order: Int, val lastUsed: Instant?)

    private val entries = MutableStateFlow<List<Entry>>(emptyList())

    override fun observeActive(): Flow<List<Activity>> = entries.map { all ->
        all.filter { it.activity.status == ActivityStatus.ACTIVE }
            .sortedWith(compareByDescending<Entry> { it.lastUsed }.thenBy { it.order })
            .map { it.activity }
    }

    override suspend fun findById(id: ActivityId): Activity? = entries.value.firstOrNull {
        it.activity.id == id
    }?.activity

    override suspend fun findByKey(key: ActivityKey): Activity? =
        entries.value.firstOrNull { it.activity.key == key }?.activity

    override suspend fun countActive(): Int = entries.value.count { it.activity.status == ActivityStatus.ACTIVE }

    override suspend fun add(activity: Activity) {
        entries.update { it + Entry(activity, order = it.size, lastUsed = null) }
    }

    override suspend fun markUsed(id: ActivityId, at: Instant) {
        entries.update { all -> all.map { if (it.activity.id == id) it.copy(lastUsed = at) else it } }
    }
}
