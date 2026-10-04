package com.stateai.domain.baseline

import com.stateai.domain.sensing.SensorSample

/**
 * Provides the personal baseline to sessions. The first time (nothing stored) it calibrates from
 * incoming samples and saves the result; afterwards every session and activity reuses it.
 */
class BaselineKeeper(
    private val repository: BaselineRepository,
    private val newCalibrator: () -> BaselineCalibrator = { BaselineCalibrator() },
) {
    private var cached: UserBaseline? = null
    private var calibrator: BaselineCalibrator? = null

    suspend fun current(): UserBaseline? = cached ?: repository.load()?.also { cached = it }

    /** Replaces the stored baseline with [change] applied to it; does nothing before calibration. */
    suspend fun refine(change: (UserBaseline) -> UserBaseline) {
        val updated = change(current() ?: return)
        repository.save(updated)
        cached = updated
    }

    /** Feeds a sample while no baseline exists. Returns the calibration progress, or null if calibrated. */
    suspend fun calibrate(sample: SensorSample): CalibrationProgress? {
        if (current() != null) return null
        val progress = (calibrator ?: newCalibrator().also { calibrator = it }).add(sample)
        if (progress is CalibrationProgress.Done) {
            repository.save(progress.baseline)
            cached = progress.baseline
            calibrator = null
        }
        return progress
    }
}
