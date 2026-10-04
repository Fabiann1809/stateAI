package com.stateai.sensors.simulation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds the scenario the simulator plays; the debug panel changes it at runtime. */
class SimulationController(private val scenarios: Scenarios = Scenarios(), initial: ScenarioId = ScenarioId.MIXED) {
    private val current = MutableStateFlow(scenarios.byId(initial))
    val scenario: StateFlow<Scenario> = current.asStateFlow()

    fun select(id: ScenarioId) {
        if (current.value.id != id) current.value = scenarios.byId(id)
    }
}
