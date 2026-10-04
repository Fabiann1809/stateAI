package com.stateai.sensors.health

import android.content.Context
import com.stateai.domain.sensing.SensorSample
import com.stateai.domain.sensing.SensorSource
import java.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Real sensors: heart rate from Health Services and movement from the accelerometer, merged into
 * one sample per [period]. Requires the heart rate permission; without it, samples carry no heart rate.
 */
class HealthServicesSensorSource(
    context: Context,
    private val clock: Clock,
    private val period: Duration = 1.seconds,
) : SensorSource {
    private val heartRateReader = HeartRateReader(context)
    private val movementReader = MovementReader(context)

    override fun samples(): Flow<SensorSample> = channelFlow {
        val aggregator = SecondAggregator()
        launch { heartRateReader.heartRates().collect(aggregator::onHeartRate) }
        launch { movementReader.magnitudes().collect(aggregator::onMovement) }
        while (true) {
            delay(period)
            send(aggregator.tick(clock.instant()))
        }
    }
}
