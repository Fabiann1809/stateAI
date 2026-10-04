package com.stateai.domain.learning

import com.stateai.domain.segment.Segment
import kotlin.time.Duration

/**
 * Updates an activity's learning after each valid session: one more session, and running means of
 * the session duration and of the clean-window movement.
 */
class ActivityProfileLearner(
    private val repository: LearningRepository,
    private val rule: ValidSessionRule = ValidSessionRule(),
) : SegmentLearner {
    override suspend fun learn(segment: Segment) {
        if (!rule.isValid(segment)) return
        val id = segment.activity.id
        val current = repository.load(id) ?: ActivityLearning(id)
        val n = current.validSessions
        repository.save(
            current.copy(
                validSessions = n + 1,
                meanDuration = runningMean(current.meanDuration, segment.duration, n),
                meanMovement = segment.cleanMovement?.let { runningMean(current.meanMovement, it, n) }
                    ?: current.meanMovement,
            ),
        )
    }

    private fun runningMean(mean: Duration?, value: Duration, count: Int): Duration =
        if (mean == null) value else mean + (value - mean) / (count + 1)

    private fun runningMean(mean: Double?, value: Double, count: Int): Double =
        if (mean == null) value else mean + (value - mean) / (count + 1)
}
