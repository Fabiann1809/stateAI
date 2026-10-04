package com.stateai.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.state.DisplayState
import com.stateai.ui.components.StateIndicator
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.labelRes
import kotlin.math.roundToInt

private const val PERCENT = 100

/** The estimated state (shape, color and name), calibration progress, or a waiting hint. */
@Composable
fun SessionStatusRow(status: MonitorStatus, displayState: DisplayState?) {
    when {
        status is MonitorStatus.Calibrating -> StatusText(
            stringResource(R.string.session_calibrating, (status.fraction * PERCENT).roundToInt()),
        )
        displayState == null -> StatusText(stringResource(R.string.session_waiting))
        else -> StateIndicator(displayState, label = stringResource(displayState.labelRes()))
    }
}

/** Ambient version: gray outline indicator, no saturated color. */
@Composable
fun AmbientStateRow(displayState: DisplayState) {
    StateIndicator(
        state = displayState,
        label = stringResource(displayState.labelRes()),
        textColor = StateAiColors.Ambient,
        ambient = true,
    )
}

@Composable
private fun StatusText(text: String) {
    Text(text = text, fontSize = StateAiDimens.Label, color = StateAiColors.Text2)
}
