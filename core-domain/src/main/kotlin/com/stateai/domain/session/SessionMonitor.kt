package com.stateai.domain.session

import com.stateai.domain.baseline.BaselineKeeper
import com.stateai.domain.baseline.CalibrationProgress
import com.stateai.domain.features.FeatureWindow
import com.stateai.domain.features.FeatureWindowStream
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.haptics.HapticPlayer
import com.stateai.domain.haptics.HapticPolicy
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.sensing.SensorSource
import com.stateai.domain.state.StateEngine
import com.stateai.domain.state.StateEstimate
import kotlin.time.toKotlinDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach

/** What the session knows about the person right now. */
sealed interface MonitorStatus {
    /** First use: collecting the resting baseline. */
    data class Calibrating(val fraction: Float) : MonitorStatus

    /** Baseline ready, waiting for the first clean window. */
    data object Waiting : MonitorStatus

    data class Estimating(val estimate: StateEstimate) : MonitorStatus
}

/**
 * Runs the estimation pipeline for a session: sensor samples → baseline calibration (first time
 * only) → feature windows → state engine → haptic policy, recording the segment summary on the way.
 * [run] suspends until cancelled.
 */
class SessionMonitor(
    private val sensorSource: SensorSource,
    private val baselineKeeper: BaselineKeeper,
    private val player: HapticPlayer,
    private val recorder: SegmentRecorder,
    private val windowStream: FeatureWindowStream = FeatureWindowStream(),
    private val newEngine: () -> StateEngine = { StateEngine() },
    private val newPolicy: () -> HapticPolicy = { HapticPolicy() },
) {
    private val current = MutableStateFlow<MonitorStatus>(MonitorStatus.Waiting)
    val status: StateFlow<MonitorStatus> = current.asStateFlow()

    suspend fun run(session: ActiveSession) {
        val engine = newEngine()
        val policy = newPolicy()
        current.value = MonitorStatus.Waiting
        val samples = sensorSource.samples().onEach { sample ->
            val progress = baselineKeeper.calibrate(sample)
            if (progress is CalibrationProgress.Collecting) current.value = MonitorStatus.Calibrating(progress.fraction)
            if (progress is CalibrationProgress.Done) current.value = MonitorStatus.Waiting
        }
        windowStream.windows(samples).collect { window ->
            val estimate = estimate(window, session, engine)
            recorder.onWindow(window, estimate)
            estimate?.let {
                current.value = MonitorStatus.Estimating(estimate)
                policy.onEstimate(estimate)?.let { event ->
                    if (event == HapticEvent.PAUSE_SUGGESTED) recorder.onPauseSuggested()
                    player.play(event)
                }
            }
        }
    }

    private suspend fun estimate(window: FeatureWindow, session: ActiveSession, engine: StateEngine): StateEstimate? {
        val baseline = baselineKeeper.current() ?: return null
        val elapsed = java.time.Duration.between(session.startedAt, window.end).toKotlinDuration()
        return engine.update(window, baseline, session.profile, elapsed)
    }
}
