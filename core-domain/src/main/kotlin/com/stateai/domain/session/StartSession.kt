package com.stateai.domain.session

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.profile.ProfileProvider
import java.time.Clock

/**
 * Starts a session for an activity, or keeps the running one if it is for the same activity.
 * Returns null when the activity does not exist.
 */
class StartSession(
    private val repository: ActivityRepository,
    private val tracker: SessionTracker,
    private val profiles: ProfileProvider,
    private val clock: Clock,
) {
    suspend operator fun invoke(activityId: ActivityId): ActiveSession? =
        tracker.activeSession.value?.takeIf { it.activity.id == activityId } ?: startNew(activityId)

    private suspend fun startNew(activityId: ActivityId): ActiveSession? {
        val activity = repository.findById(activityId) ?: return null
        val now = clock.instant()
        val session = ActiveSession(activity, profiles.targetBlockFor(activity), startedAt = now)
        repository.markUsed(activityId, now)
        tracker.start(session)
        return session
    }
}
