package com.stateai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.Text
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.tabular
import kotlin.math.roundToInt

/**
 * One score component: icon, name, value 0-100 and, when yesterday has a score, the change since
 * then (accent when it went up, gray when it went down, so a drop is not shown as an alarm).
 */
@Composable
fun FactorCard(icon: ImageVector, name: String, value: Double?, change: Double?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .background(StateAiColors.Surface2, RoundedCornerShape(CARD_HEIGHT / 2))
            .padding(start = StateAiDimens.SpaceS, end = StateAiDimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon)
        Text(
            name,
            fontSize = StateAiDimens.Label,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value?.roundToInt()?.toString() ?: "–",
                fontSize = VALUE_SIZE,
                fontWeight = FontWeight.SemiBold,
                style = LocalTextStyle.current.tabular(),
            )
            change?.roundToInt()?.takeIf { it != 0 }?.let { delta ->
                Text(
                    text = if (delta > 0) "+$delta" else "−${-delta}",
                    fontSize = StateAiDimens.Label,
                    color = if (delta > 0) StateAiColors.Accent else DROP,
                    style = LocalTextStyle.current.tabular(),
                )
            }
        }
    }
}

/** The icon on a soft turquoise disc. */
@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        Modifier.size(TILE_SIZE).background(StateAiColors.AccentSoft, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = StateAiColors.Accent, modifier = Modifier.size(ICON_SIZE))
    }
}

private val CARD_HEIGHT = 46.dp
private val TILE_SIZE = 28.dp
private val ICON_SIZE = 14.dp
private val VALUE_SIZE = 17.sp
private val DROP = Color(0xFF7F92B5)
