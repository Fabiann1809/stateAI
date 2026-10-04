package com.stateai.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.session.MonitorStatus
import com.stateai.ui.common.color
import com.stateai.ui.common.labelRes
import kotlin.math.roundToInt

private const val PERCENT = 100

/** Estimated level (colored dot and name), calibration progress, or a waiting hint. */
@Composable
fun SessionStatusRow(status: MonitorStatus) {
    when (status) {
        is MonitorStatus.Calibrating -> StatusText(
            stringResource(R.string.session_calibrating, (status.fraction * PERCENT).roundToInt()),
        )
        MonitorStatus.Waiting -> StatusText(stringResource(R.string.session_waiting))
        is MonitorStatus.Estimating -> Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(10.dp).background(status.estimate.level.color(), CircleShape))
            StatusText(stringResource(status.estimate.level.labelRes()))
            if (status.estimate.restless) StatusText(stringResource(R.string.level_restless))
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall)
}
