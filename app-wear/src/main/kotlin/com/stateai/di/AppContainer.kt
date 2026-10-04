package com.stateai.di

import android.content.Context
import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.haptics.HapticPlayer
import com.stateai.domain.profile.CategoryDefaultsProfileProvider
import com.stateai.domain.profile.ProfileProvider
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import com.stateai.haptics.VibratorHapticPlayer
import java.time.Clock

/** Creates and holds the app-wide dependencies (manual dependency injection). */
class AppContainer(context: Context) {
    val clock: Clock = Clock.systemUTC()
    val activityRepository: ActivityRepository = InMemoryActivityRepository()
    val sessionTracker = SessionTracker()
    val hapticPlayer: HapticPlayer = VibratorHapticPlayer(context)
    private val profileProvider: ProfileProvider = CategoryDefaultsProfileProvider()

    val startSession = StartSession(activityRepository, sessionTracker, profileProvider, clock)
}
