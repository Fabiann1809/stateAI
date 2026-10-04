package com.stateai.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.energy.EnergyBudget
import kotlin.math.roundToInt

/** Estimated energy, plus a gentle suggestion (never a lock) when it is low. */
@Composable
fun EnergyText(energy: EnergyBudget) {
    Text(
        text = stringResource(R.string.energy_level, energy.level.roundToInt()),
        style = MaterialTheme.typography.labelSmall,
    )
    if (energy.isLow) {
        Text(
            text = stringResource(R.string.energy_low_suggestion),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}
