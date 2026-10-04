package com.stateai.di

import android.content.Context
import com.stateai.BuildConfig
import com.stateai.domain.sensing.SensorSource
import com.stateai.sensors.health.HealthServicesSensorSource
import com.stateai.sensors.simulation.SimulatedSensorSource
import com.stateai.sensors.simulation.SimulationController
import java.time.Clock

/** The sensor source chosen at build time (`-Pstateai.sensorSource`), simulated by default. */
class SensorsModule(context: Context, clock: Clock) {
    val simulationController = SimulationController()
    val source: SensorSource = if (BuildConfig.USE_HEALTH_SERVICES) {
        HealthServicesSensorSource(context, clock)
    } else {
        SimulatedSensorSource(simulationController.scenario, clock)
    }
}
