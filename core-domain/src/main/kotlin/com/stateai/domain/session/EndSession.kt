package com.stateai.domain.session

import com.stateai.domain.learning.SegmentLearner
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock

/** Ends the running session: closes its segment, stores it, learns from it and clears the tracker. */
class EndSession(
    private val tracker: SessionTracker,
    private val recorder: SegmentRecorder,
    private val segments: SegmentRepository,
    private val clock: Clock,
    private val learners: List<SegmentLearner> = emptyList(),
) {
    suspend operator fun invoke(): Segment? {
        val segment = recorder.finish(clock.instant())
        segment?.let {
            segments.save(it)
            learners.forEach { learner -> learner.learn(it) }
        }
        tracker.stop()
        return segment
    }
}
