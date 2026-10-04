package com.stateai.domain.state.model

import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.features.FeatureWindow
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Inputs of the learned classifier, in the exact order of `INPUT_COLUMNS` in
 * `ml-python/src/stateai_ml/model.py`. Standardization is folded into the exported model.
 */
object ModelInputs {
    val NAMES = listOf(
        "heart_rate_delta",
        "heart_rate_std_dev",
        "heart_rate_mean_abs_diff",
        "mean_movement",
        "fidget_count",
        "elapsed_minutes",
    )

    /** Returns null when the window has no heart rate. */
    fun of(window: FeatureWindow, baseline: UserBaseline, elapsedInSession: Duration): FloatArray? {
        val meanHeartRate = window.meanHeartRate ?: return null
        return floatArrayOf(
            (meanHeartRate - baseline.restingHeartRate).toFloat(),
            window.heartRateStdDev.toFloat(),
            window.heartRateMeanAbsDiff.toFloat(),
            window.meanMovement.toFloat(),
            window.fidgetCount.toFloat(),
            elapsedInSession.toDouble(DurationUnit.MINUTES).toFloat(),
        )
    }
}
