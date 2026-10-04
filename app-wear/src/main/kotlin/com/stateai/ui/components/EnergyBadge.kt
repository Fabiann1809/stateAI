package com.stateai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.energy.EnergyBudget
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.tabular
import kotlin.math.roundToInt

/** Compact energy: a small silhouette and the percentage, always shown together (never color alone). */
@Composable
fun EnergyBadge(
    energy: EnergyBudget,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 33.dp,
    fontSize: TextUnit = 20.sp,
    ambient: Boolean = false,
) {
    val percent = energy.level.roundToInt()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EnergySilhouette(
            level = energy.level,
            color = color,
            height = height,
            style = if (ambient) SilhouetteStyle.AMBIENT else SilhouetteStyle.FULL,
            low = energy.isLow,
            description = stringResource(R.string.energy_description, percent),
        )
        Text(
            text = stringResource(R.string.percent, percent),
            fontSize = fontSize,
            fontWeight = if (ambient) FontWeight.Normal else FontWeight.Medium,
            color = if (ambient) StateAiColors.Ambient else StateAiColors.Text1,
            style = LocalTextStyle.current.tabular(),
        )
    }
}
