package com.stateai.ui.debug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R

/** Entry point to the debug tools (debug builds only). */
@Composable
fun DebugMenuScreen(onOpenHaptics: () -> Unit, onOpenSensors: () -> Unit) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.debug_open)) } }
            item {
                Button(
                    onClick = onOpenSensors,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.debug_sensors_title)) },
                )
            }
            item {
                Button(
                    onClick = onOpenHaptics,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.debug_haptics_title)) },
                )
            }
        }
    }
}
