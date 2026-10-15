package com.stateai.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.state.DisplayState
import com.stateai.ui.components.StateIndicator
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.color
import com.stateai.ui.theme.labelRes
import kotlin.math.roundToInt

private const val PERCENT = 100
private const val PILL_ALPHA = 0.12f
private val PILL_PADDING = 10.dp
private val PILL_VERTICAL_PADDING = 3.dp

/** The estimated state (shape, color and name), calibration progress, or a waiting hint. */
@Composable
fun SessionStatusRow(status: MonitorStatus, displayState: DisplayState?) {
    when {
        status is MonitorStatus.Calibrating -> StatusText(
            stringResource(R.string.session_calibrating, (status.fraction * PERCENT).roundToInt()),
        )
        displayState == null -> StatusText(stringResource(R.string.session_waiting))
        else -> StatePill(displayState)
    }
}

/** The state as a capsule tinted with its own color: shape, name and a soft background. */
@Composable
private fun StatePill(displayState: DisplayState) {
    StateIndicator(
        state = displayState,
        label = stringResource(displayState.labelRes()),
        modifier = Modifier
            .background(displayState.color().copy(alpha = PILL_ALPHA), CircleShape)
            .padding(horizontal = PILL_PADDING, vertical = PILL_VERTICAL_PADDING),
    )
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
    Text(text = text, fontSize = StateAiDimens.Label, fontWeight = FontWeight.Medium, color = StateAiColors.Accent)
}
