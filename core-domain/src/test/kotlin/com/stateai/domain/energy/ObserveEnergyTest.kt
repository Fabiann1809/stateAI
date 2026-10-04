package com.stateai.domain.energy

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.learning.ObserveFocusProfile
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.session.ActiveSession
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import com.stateai.domain.testing.FakeSegmentRepository
import com.stateai.domain.testing.MutableClock
import com.stateai.domain.testing.testSegment
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveEnergyTest {
    private val clock = MutableClock(Instant.parse("2026-10-04T12:00:00Z"))
    private val segments = FakeSegmentRepository()
    private val recorder = SegmentRecorder { SegmentId("running") }
    private val observe = ObserveEnergy(segments, ObserveFocusProfile(segments, clock), recorder, clock)

    @Test
    fun `counts today's stored segments and the session in progress`() = runTest {
        segments.save(
            testSegment(
                start = Instant.parse("2026-10-04T09:00:00Z"),
                duration = 60.minutes,
                trace = LevelTrace("H".repeat(60)),
            ),
        )
        segments.save(
            testSegment(
                id = "yesterday",
                start = Instant.parse("2026-10-03T09:00:00Z"),
                trace = LevelTrace("H".repeat(40)),
            ),
        )
        val activity = Activity(ActivityId("a"), ActivityCategory.STUDY, null)
        recorder.start(
            ActiveSession(activity, DefaultCategoryProfiles.STUDY, startedAt = clock.instant().minusSeconds(600)),
        )
        repeat(10) {
            recorder.onWindow(
                window(clock.instant().minusSeconds(540L - it * 60)),
                StateEstimate(ActivationLevel.LOW, false),
            )
        }

        val budget = observe(flowOf(clock.instant())).first()

        assertEquals(18.0 + 1.0, budget.consumed, 1e-9)
    }

    private fun window(end: Instant) = FeatureWindow(end.minusSeconds(180), end, 180, 180, 65.0, 1.0, 1.0, 0.05, 0, 0.0)
}
