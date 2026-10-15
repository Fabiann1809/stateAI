package com.stateai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.stateai.ui.theme.StateAiColors

private val ICON_GAP = 6.dp
private val ICON_SIZE = 15.dp
private val PRIMARY_HEIGHT = 44.dp
private val SECONDARY_HEIGHT = 36.dp
private val PADDING = 22.dp
private val GLOW_ELEVATION = 10.dp
private val BORDER = 1.dp

/** The main action of a screen: a turquoise-to-violet pill with dark text and a soft glow. */
@Composable
fun GradientButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Box(
        modifier = modifier
            .height(PRIMARY_HEIGHT)
            .defaultMinSize(minWidth = PRIMARY_HEIGHT)
            .shadow(
                GLOW_ELEVATION,
                CircleShape,
                ambientColor = StateAiColors.AccentGlow,
                spotColor = StateAiColors.AccentGlow,
            )
            .clip(CircleShape)
            .background(StateAiColors.AccentGradient)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PADDING),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(ICON_GAP), verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(it, contentDescription = null, tint = StateAiColors.OnAccent, modifier = Modifier.size(ICON_SIZE))
            }
            Text(label, color = StateAiColors.OnAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** A secondary action: a translucent pill with a hairline border. */
@Composable
fun GlassButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(SECONDARY_HEIGHT)
            .defaultMinSize(minWidth = SECONDARY_HEIGHT)
            .clip(CircleShape)
            .background(StateAiColors.Surface2)
            .border(BorderStroke(BORDER, StateAiColors.Outline), CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PADDING),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = StateAiColors.Text1, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** The closing action of a confirmation: a lavender pill with dark text. */
@Composable
fun LavenderButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(SECONDARY_HEIGHT)
            .defaultMinSize(minWidth = SECONDARY_HEIGHT)
            .clip(CircleShape)
            .background(StateAiColors.Accent2)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PADDING),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = StateAiColors.OnAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** The quietest action ("Ahora no"): only text, with a 48 dp touch target. */
@Composable
fun TextAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = StateAiColors.Text2) {
    Box(
        modifier = modifier
            .height(TEXT_ACTION_HEIGHT)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PADDING),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = color, fontSize = 13.sp)
    }
}

private val TEXT_ACTION_HEIGHT = 40.dp
