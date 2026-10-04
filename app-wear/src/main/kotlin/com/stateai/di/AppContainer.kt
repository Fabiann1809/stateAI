package com.stateai.di

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.profile.CategoryDefaultsProfileProvider
import com.stateai.domain.profile.ProfileProvider
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import java.time.Clock

/** Creates and holds the app-wide dependencies (manual dependency injection). */
class AppContainer {
    val clock: Clock = Clock.systemUTC()
    val activityRepository: ActivityRepository = InMemoryActivityRepository()
    val sessionTracker = SessionTracker()
    private val profileProvider: ProfileProvider = CategoryDefaultsProfileProvider()

    val startSession = StartSession(activityRepository, sessionTracker, profileProvider, clock)
}
