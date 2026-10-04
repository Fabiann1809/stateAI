package com.stateai.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActivityEntity::class, SegmentEntity::class, PauseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class StateAiDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun segmentDao(): SegmentDao

    companion object {
        private const val NAME = "stateai.db"

        fun create(context: Context): StateAiDatabase =
            Room.databaseBuilder(context, StateAiDatabase::class.java, NAME).build()
    }
}
