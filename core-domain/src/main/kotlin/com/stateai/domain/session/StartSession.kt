package com.stateai.domain.session

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.profile.ProfileProvider
import com.stateai.domain.segment.SegmentRecorder
import java.time.Clock

/**
 * Starts a session for an activity, or keeps the running one if it is for the same activity.
 * Switching to another activity closes the running segment first. Returns null for an unknown activity.
 */
class StartSession(
    private val repository: ActivityRepository,
    private val tracker: SessionTracker,
    private val profiles: ProfileProvider,
    private val recorder: SegmentRecorder,
    private val endSession: EndSession,
    private val clock: Clock,
) {
    suspend operator fun invoke(activityId: ActivityId): ActiveSession? =
        tracker.activeSession.value?.takeIf { it.activity.id == activityId } ?: startNew(activityId)

    private suspend fun startNew(activityId: ActivityId): ActiveSession? {
        val activity = repository.findById(activityId) ?: return null
        if (tracker.activeSession.value != null) endSession()
        val now = clock.instant()
        val session = ActiveSession(activity, profiles.profileFor(activity), startedAt = now)
        repository.markUsed(activityId, now)
        recorder.start(session)
        tracker.start(session)
        return session
    }
}
