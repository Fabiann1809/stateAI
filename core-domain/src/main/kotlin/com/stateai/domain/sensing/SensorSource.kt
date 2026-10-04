package com.stateai.domain.sensing

import kotlinx.coroutines.flow.Flow

/**
 * Source of physiological and movement samples. The domain only depends on this interface;
 * implementations are a scripted simulator and the Health Services adapter.
 */
fun interface SensorSource {
    /**
     * Cold flow of samples, about one per second, starting when collected and stopping when
     * the collector is cancelled. Real sources release their sensors on cancellation.
     */
    fun samples(): Flow<SensorSample>
}
