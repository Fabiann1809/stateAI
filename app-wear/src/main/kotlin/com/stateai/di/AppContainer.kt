package com.stateai.di

import android.content.Context
import com.stateai.data.LocalStorage
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.baseline.BaselineKeeper
import com.stateai.domain.learning.ActivityProfileLearner
import com.stateai.domain.learning.BaselineLearner
import com.stateai.domain.learning.FeedbackSensitivityLearner
import com.stateai.domain.learning.RecordFeedback
import com.stateai.domain.pause.GuidedPause
import com.stateai.domain.profile.LearnedProfileProvider
import com.stateai.domain.profile.ProfileProvider
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.segment.SegmentRepository
import com.stateai.domain.session.EndSession
import com.stateai.domain.session.SessionMonitor
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import com.stateai.domain.state.StateEngine
import java.time.Clock
import java.time.ZoneId
import java.util.UUID

/** Creates and holds the app-wide dependencies (manual dependency injection). */
class AppContainer(context: Context) {
    val clock: Clock = Clock.systemUTC()
    private val localClock: Clock = clock.withZone(ZoneId.systemDefault())
    private val storage = LocalStorage(context, clock)

    val activityRepository: ActivityRepository = storage.activities
    val segmentRepository: SegmentRepository = storage.segments
    val sessionTracker = SessionTracker()

    val haptics = HapticsModule(context, clock, sessionTracker)
    val sensors = SensorsModule(context, clock)

    // Sessions
    private val profileProvider: ProfileProvider = LearnedProfileProvider(storage.learning)
    private val baselineKeeper = BaselineKeeper(storage.baseline)
    val segmentRecorder = SegmentRecorder { SegmentId(UUID.randomUUID().toString()) }
    val insights =
        InsightsModule(context, segmentRepository, storage.learning, segmentRecorder, localClock, haptics.player)
    val createActivity = CreateActivity(activityRepository) { ActivityId(UUID.randomUUID().toString()) }
    val guidedPause = GuidedPause(segmentRecorder, clock)
    val recordFeedback = RecordFeedback(segmentRepository, FeedbackSensitivityLearner(storage.learning))
    val endSession = EndSession(
        sessionTracker,
        segmentRecorder,
        segmentRepository,
        clock,
        learners = listOf(BaselineLearner(baselineKeeper), ActivityProfileLearner(storage.learning)),
    )
    val startSession =
        StartSession(activityRepository, sessionTracker, profileProvider, segmentRecorder, endSession, clock)
    private val classifiers = ClassifierModule(context)
    val sessionMonitor = SessionMonitor(
        sensors.source,
        baselineKeeper,
        haptics.sessionPlayer,
        segmentRecorder,
        newEngine = { StateEngine(classifiers.classifier) },
    )
}
