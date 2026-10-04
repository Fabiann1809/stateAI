package com.stateai.ui.debug

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.haptics.HapticEvent

/** Debug-only screen that plays each haptic pattern on demand. */
@Composable
fun HapticsDebugScreen() {
    val player = appContainer().haptics.player
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.debug_haptics_title)) } }
            items(HapticEvent.entries) { event ->
                Button(
                    onClick = { player.play(event) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(event.labelRes())) },
                )
            }
        }
    }
}

@StringRes
private fun HapticEvent.labelRes(): Int = when (this) {
    HapticEvent.BLOCK_START -> R.string.haptic_block_start
    HapticEvent.BREATHE -> R.string.haptic_breathe
    HapticEvent.PAUSE_SUGGESTED -> R.string.haptic_pause_suggested
    HapticEvent.OVERLOAD_ALERT -> R.string.haptic_overload_alert
}
