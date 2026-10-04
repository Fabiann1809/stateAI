package com.stateai.domain.learning

import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.testing.FakeLearningRepository
import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.testSegment
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecordFeedbackTest {
    @Test
    fun `stores the feedback and adjusts the activity`() = runTest {
        val segments = FakeSegmentRepository()
        val learning = FakeLearningRepository()
        val segment = testSegment(id = "s", levelTime = LevelDurations(low = 40.minutes))
        segments.save(segment)

        RecordFeedback(segments, FeedbackSensitivityLearner(learning))(SegmentId("s"), Feedback.BAD)

        assertEquals(Feedback.BAD, segments.find(SegmentId("s"))?.feedback)
        assertEquals(-0.02, learning.stored.getValue(segment.activity.id).sensitivityAdjustment, 1e-9)
    }
}
