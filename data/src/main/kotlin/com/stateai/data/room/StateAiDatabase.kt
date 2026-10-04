package com.stateai.data.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActivityEntity::class, SegmentEntity::class, PauseEntity::class, ActivityLearningEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class StateAiDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun segmentDao(): SegmentDao

    abstract fun learningDao(): LearningDao

    companion object {
        private const val NAME = "stateai.db"

        fun create(context: Context): StateAiDatabase =
            Room.databaseBuilder(context, StateAiDatabase::class.java, NAME).build()
    }
}
