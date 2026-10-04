package com.stateai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.energy.EnergyBand
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.state.DisplayState
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.color
import com.stateai.ui.theme.labelRes
import com.stateai.ui.theme.tabular
import com.stateai.ui.theme.textColor
import kotlin.math.roundToInt

/**
 * The large energy silhouette with its percentage and band, the current state when there is one,
 * and how much was spent and recovered today. Used in the session and in the day summary.
 */
@Composable
fun EnergyOverview(energy: EnergyBudget, displayState: DisplayState?) {
    val percent = energy.level.roundToInt()
    Row(
        horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EnergySilhouette(
            level = energy.level,
            color = displayState?.color() ?: StateAiColors.Accent,
            height = SILHOUETTE_HEIGHT,
            low = energy.isLow,
            description = stringResource(R.string.energy_description, percent),
        )
        EnergyDetails(energy, displayState)
    }
}

@Composable
private fun EnergyDetails(energy: EnergyBudget, displayState: DisplayState?) {
    val band = EnergyBand.of(energy)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = stringResource(R.string.percent, energy.level.roundToInt()),
            fontSize = PERCENT_SIZE,
            fontWeight = FontWeight.Medium,
            style = LocalTextStyle.current.tabular(),
        )
        Text(stringResource(band.labelRes()), fontSize = StateAiDimens.Label, color = band.textColor())
        displayState?.let { state ->
            StateIndicator(
                state = state,
                label = stringResource(state.labelRes()),
                size = 12.dp,
                fontSize = StateAiDimens.Label,
            )
        }
        Spacer(Modifier.height(StateAiDimens.SpaceS))
        Text(
            text = stringResource(
                R.string.energy_spent_recovered,
                energy.consumed.roundToInt(),
                energy.recovered.roundToInt(),
            ),
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text2,
        )
    }
}

private val SILHOUETTE_HEIGHT = 150.dp
private val PERCENT_SIZE = 33.sp
