package com.stateai.sensors.simulation

import com.stateai.domain.baseline.BaselineCalibrator
import com.stateai.domain.baseline.CalibrationProgress
import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.features.FeatureWindowStream
import com.stateai.domain.profile.DefaultCategoryProfiles
import com.stateai.domain.sensing.SensorSample
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.RuleBasedClassifier
import com.stateai.domain.state.StateEngine
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** End-to-end check that each scripted scenario yields the activation levels it was designed for. */
class ScenarioClassificationTest {
    private val start = Instant.parse("2026-10-04T09:00:00Z")
    private val scenarios = Scenarios()
    private val classifier = RuleBasedClassifier()

    @Test
    fun `deep focus stays low and calm`() = runTest {
        val estimates = estimatesFor(ScenarioId.DEEP_FOCUS)

        assertTrue(estimates.all { it.level == ActivationLevel.LOW && !it.restless }, "$estimates")
    }

    @Test
    fun `overload ends in high activation`() = runTest {
        val estimates = estimatesFor(ScenarioId.OVERLOAD)

        assertTrue(estimates.first().level == ActivationLevel.LOW)
        assertTrue(estimates.takeLast(4).all { it.level == ActivationLevel.HIGH }, "$estimates")
    }

    @Test
    fun `fatigue ends restless`() = runTest {
        val estimates = estimatesFor(ScenarioId.FATIGUE)

        assertTrue(estimates.take(15).none { it.restless })
        assertTrue(estimates.takeLast(5).all { it.restless }, "$estimates")
    }

    @Test
    fun `mixed session goes from low to high and back down`() = runTest {
        val levels = estimatesFor(ScenarioId.MIXED).map { it.level }

        assertTrue(levels.take(8).all { it == ActivationLevel.LOW }, "$levels")
        assertTrue(ActivationLevel.HIGH in levels.subList(14, 20), "$levels")
        assertTrue(levels[23] != ActivationLevel.HIGH, "$levels")
    }

    @Test
    fun `smoothed mixed session does not flicker`() = runTest {
        val engine = StateEngine()
        val smoothed = estimatesFor(ScenarioId.MIXED) { window, baseline, elapsed ->
            engine.onWindow(window, baseline, DefaultCategoryProfiles.STUDY, elapsed)
        }.map { it.level }

        val flickers = smoothed.windowed(3).count { (a, b, c) -> a == c && a != b }
        assertTrue(flickers == 0, "$smoothed")
        val phaseChanges = scenarios.byId(ScenarioId.MIXED).phases.size - 1
        assertTrue(smoothed.zipWithNext().count { (a, b) -> a != b } <= phaseChanges * 2, "$smoothed")
    }

    private suspend fun estimatesFor(
        id: ScenarioId,
        estimate: (FeatureWindow, UserBaseline, Duration) -> StateEstimate? = { window, baseline, elapsed ->
            classifier.classify(window, baseline, DefaultCategoryProfiles.STUDY, elapsed)
        },
    ): List<StateEstimate> {
        val scenario = scenarios.byId(id)
        val signal = ScenarioSignal(scenario)
        val samples = (0 until scenario.totalDuration.inWholeSeconds).map { second ->
            val value = signal.valueAt(second)
            SensorSample(start.plusSeconds(second), value.heartRateBpm, value.movement)
        }
        val calibrator = BaselineCalibrator()
        val baseline = samples.asSequence().map(calibrator::add).filterIsInstance<CalibrationProgress.Done>()
            .first().baseline
        return FeatureWindowStream().windows(samples.asFlow()).toList().mapNotNull { window ->
            estimate(window, baseline, java.time.Duration.between(start, window.end).toKotlinDuration())
        }
    }
}
