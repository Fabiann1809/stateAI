package com.stateai.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.stateai.data.baseline.DataStoreBaselineRepository
import com.stateai.data.room.RoomActivityRepository
import com.stateai.data.room.RoomLearningRepository
import com.stateai.data.room.RoomSegmentRepository
import com.stateai.data.room.StateAiDatabase
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.baseline.BaselineRepository
import com.stateai.domain.learning.LearningRepository
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock

/** On-device storage. Callers only see domain repository interfaces, never Room or DataStore. */
class LocalStorage(context: Context, clock: Clock) {
    private val database = StateAiDatabase.create(context)

    val activities: ActivityRepository = RoomActivityRepository(database.activityDao(), clock)
    val segments: SegmentRepository = RoomSegmentRepository(database.segmentDao())
    val learning: LearningRepository = RoomLearningRepository(database.learningDao())
    val baseline: BaselineRepository = DataStoreBaselineRepository(
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(BASELINE_STORE) },
    )

    private companion object {
        const val BASELINE_STORE = "baseline"
    }
}
