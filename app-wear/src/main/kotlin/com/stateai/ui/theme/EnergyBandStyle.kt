package com.stateai.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.stateai.R
import com.stateai.domain.energy.EnergyBand

@StringRes
fun EnergyBand.labelRes(): Int = when (this) {
    EnergyBand.HIGH -> R.string.energy_band_high
    EnergyBand.MEDIUM -> R.string.energy_band_medium
    EnergyBand.LOW -> R.string.energy_band_low
}

/** Low energy is the only band with its own (soft pink) color; the others stay neutral. */
fun EnergyBand.textColor(): Color = if (this == EnergyBand.LOW) StateAiColors.Exhausted else StateAiColors.Text2
