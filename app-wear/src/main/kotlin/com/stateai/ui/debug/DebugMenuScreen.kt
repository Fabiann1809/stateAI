package com.stateai.ui.debug

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import kotlinx.coroutines.launch

/** Entry point to the debug tools (debug builds only). */
@Composable
fun DebugMenuScreen(onOpenHaptics: () -> Unit, onOpenSensors: () -> Unit) {
    val exporter = appContainer().insights.summaryExporter
    val scope = rememberCoroutineScope()
    var exported by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.debug_open)) } }
            item { MenuButton(R.string.debug_sensors_title, onOpenSensors) }
            item { MenuButton(R.string.debug_haptics_title, onOpenHaptics) }
            item {
                val label = if (exported) R.string.debug_export_done else R.string.debug_export
                MenuButton(label) { scope.launch { exporter.export().also { exported = true } } }
            }
        }
    }
}

@Composable
private fun MenuButton(@StringRes label: Int, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(label)) })
}
