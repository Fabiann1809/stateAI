package com.stateai.domain.learning

import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRepository

/** Stores the one-tap feedback of a segment and lets the activity learn from it. */
class RecordFeedback(private val segments: SegmentRepository, private val learner: FeedbackSensitivityLearner) {
    suspend operator fun invoke(id: SegmentId, feedback: Feedback) {
        segments.setFeedback(id, feedback)
        segments.find(id)?.let { learner.learn(it, feedback) }
    }
}
