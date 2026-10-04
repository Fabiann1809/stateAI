package com.stateai.sensors.simulation

import com.stateai.domain.sensing.SensorSample
import com.stateai.domain.sensing.SensorSource
import java.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Plays the selected scenario at one sample per [period]. Changing [scenario] restarts the signal
 * from the beginning of the new scenario.
 */
class SimulatedSensorSource(
    private val scenario: StateFlow<Scenario>,
    private val clock: Clock,
    private val period: Duration = 1.seconds,
    private val seed: Long = ScenarioSignal.DEFAULT_SEED,
) : SensorSource {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun samples(): Flow<SensorSample> = scenario.flatMapLatest(::play)

    private fun play(scenario: Scenario): Flow<SensorSample> = flow {
        val signal = ScenarioSignal(scenario, seed)
        var second = 0L
        while (true) {
            val value = signal.valueAt(second++)
            emit(SensorSample(clock.instant(), value.heartRateBpm, value.movement))
            delay(period)
        }
    }
}
