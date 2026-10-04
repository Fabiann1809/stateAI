package com.stateai.di

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.domain.activity.ActivityRepository
import java.time.Clock

/** Creates and holds the app-wide dependencies (manual dependency injection). */
class AppContainer {
    val clock: Clock = Clock.systemUTC()
    val activityRepository: ActivityRepository = InMemoryActivityRepository()
}
