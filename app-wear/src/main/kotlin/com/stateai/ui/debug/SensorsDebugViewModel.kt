package com.stateai.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.sensing.SensorSample
import com.stateai.domain.sensing.SensorSource
import com.stateai.sensors.simulation.ScenarioId
import com.stateai.sensors.simulation.SimulationController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

data class SensorsDebugUiState(val selected: ScenarioId? = null, val latest: SensorSample? = null)

class SensorsDebugViewModel(private val controller: SimulationController, source: SensorSource) : ViewModel() {
    val uiState: StateFlow<SensorsDebugUiState> = combine(
        controller.scenario,
        source.samples().onStart<SensorSample?> { emit(null) },
    ) { scenario, sample -> SensorsDebugUiState(selected = scenario.id, latest = sample) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SensorsDebugUiState())

    fun select(id: ScenarioId) = controller.select(id)
}
