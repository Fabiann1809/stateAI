package com.stateai.data.baseline

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.stateai.domain.baseline.BaselineRepository
import com.stateai.domain.baseline.UserBaseline
import java.time.Instant
import kotlinx.coroutines.flow.first

/** Stores the personal baseline in a Preferences DataStore. */
class DataStoreBaselineRepository(private val dataStore: DataStore<Preferences>) : BaselineRepository {
    override suspend fun load(): UserBaseline? {
        val preferences = dataStore.data.first()
        val restingHeartRate = preferences[RESTING_HEART_RATE] ?: return null
        return UserBaseline(
            restingHeartRate = restingHeartRate,
            heartRateMeanAbsDiff = preferences[MEAN_ABS_DIFF] ?: 0.0,
            updatedAt = Instant.ofEpochMilli(preferences[UPDATED_AT] ?: 0L),
        )
    }

    override suspend fun save(baseline: UserBaseline) {
        dataStore.edit {
            it[RESTING_HEART_RATE] = baseline.restingHeartRate
            it[MEAN_ABS_DIFF] = baseline.heartRateMeanAbsDiff
            it[UPDATED_AT] = baseline.updatedAt.toEpochMilli()
        }
    }

    private companion object {
        val RESTING_HEART_RATE = doublePreferencesKey("resting_heart_rate")
        val MEAN_ABS_DIFF = doublePreferencesKey("heart_rate_mean_abs_diff")
        val UPDATED_AT = longPreferencesKey("baseline_updated_at")
    }
}
