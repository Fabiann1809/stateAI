package com.stateai.ui.debug

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.sensing.SensorSample
import com.stateai.sensors.simulation.ScenarioId

/** Debug-only screen to pick the simulated scenario and watch the live signal. */
@Composable
fun SensorsDebugScreen() {
    val container = appContainer()
    val viewModel: SensorsDebugViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SensorsDebugViewModel(container.sensors.simulationController, container.sensors.source) }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.debug_sensors_title)) } }
            item { Text(liveText(state.latest)) }
            items(ScenarioId.entries) { id ->
                ScenarioButton(id = id, selected = id == state.selected, onClick = { viewModel.select(id) })
            }
        }
    }
}

@Composable
private fun ScenarioButton(id: ScenarioId, selected: Boolean, onClick: () -> Unit) {
    val label: @Composable () -> Unit = { Text(stringResource(id.labelRes())) }
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), label = { label() })
    } else {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), label = { label() })
    }
}

@Composable
private fun liveText(sample: SensorSample?): String {
    if (sample == null) return stringResource(R.string.debug_sensors_waiting)
    val heartRate = sample.heartRateBpm?.let { "%.0f".format(it) } ?: "-"
    return stringResource(R.string.debug_sensors_live, heartRate, "%.2f".format(sample.movement))
}

@StringRes
private fun ScenarioId.labelRes(): Int = when (this) {
    ScenarioId.DEEP_FOCUS -> R.string.scenario_deep_focus
    ScenarioId.OVERLOAD -> R.string.scenario_overload
    ScenarioId.FATIGUE -> R.string.scenario_fatigue
    ScenarioId.MIXED -> R.string.scenario_mixed
}
