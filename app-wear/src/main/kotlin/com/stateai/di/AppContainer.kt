package com.stateai.di

import android.content.Context
import com.stateai.BuildConfig
import com.stateai.data.LocalStorage
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.baseline.BaselineKeeper
import com.stateai.domain.haptics.HapticPlayer
import com.stateai.domain.haptics.HapticRateLimiter
import com.stateai.domain.haptics.RateLimitedHapticPlayer
import com.stateai.domain.pause.GuidedPause
import com.stateai.domain.profile.CategoryDefaultsProfileProvider
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.profile.ProfileProvider
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.segment.SegmentRepository
import com.stateai.domain.sensing.SensorSource
import com.stateai.domain.session.EndSession
import com.stateai.domain.session.SessionMonitor
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import com.stateai.domain.summary.ObserveDaySummary
import com.stateai.haptics.VibratorHapticPlayer
import com.stateai.sensors.health.HealthServicesSensorSource
import com.stateai.sensors.simulation.SimulatedSensorSource
import com.stateai.sensors.simulation.SimulationController
import java.time.Clock
import java.time.ZoneId
import java.util.UUID

/** Creates and holds the app-wide dependencies (manual dependency injection). */
class AppContainer(context: Context) {
    val clock: Clock = Clock.systemUTC()

    // Activities and sessions
    private val storage = LocalStorage(context, clock)
    val activityRepository: ActivityRepository = storage.activities
    val sessionTracker = SessionTracker()
    private val profileProvider: ProfileProvider = CategoryDefaultsProfileProvider()
    val createActivity = CreateActivity(activityRepository) { ActivityId(UUID.randomUUID().toString()) }
    val segmentRepository: SegmentRepository = storage.segments
    private val segmentRecorder = SegmentRecorder { SegmentId(UUID.randomUUID().toString()) }
    val guidedPause = GuidedPause(segmentRecorder, clock)
    val endSession = EndSession(sessionTracker, segmentRecorder, segmentRepository, clock)
    val startSession =
        StartSession(activityRepository, sessionTracker, profileProvider, segmentRecorder, endSession, clock)

    val observeDaySummary = ObserveDaySummary(segmentRepository, clock.withZone(ZoneId.systemDefault()))

    // Sensors
    val simulationController = SimulationController()
    val sensorSource: SensorSource = if (BuildConfig.USE_HEALTH_SERVICES) {
        HealthServicesSensorSource(context, clock)
    } else {
        SimulatedSensorSource(simulationController.scenario, clock)
    }

    // Haptics
    /** Plays every event; used by the debug screen. */
    val hapticPlayer: HapticPlayer = VibratorHapticPlayer(context)

    /** Plays events within the rate limits of the running session's profile. */
    val sessionHapticPlayer: HapticPlayer = RateLimitedHapticPlayer(
        delegate = hapticPlayer,
        limiter = HapticRateLimiter(clock),
        maxPerHour = {
            sessionTracker.activeSession.value?.profile?.maxVibrationsPerHour
                ?: DefaultCategoryProfiles.OTHER.maxVibrationsPerHour
        },
    )

    // State estimation
    val sessionMonitor = SessionMonitor(
        sensorSource,
        BaselineKeeper(storage.baseline),
        sessionHapticPlayer,
        segmentRecorder,
    )
}
