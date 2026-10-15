package com.stateai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

private val GLOW_ELEVATION = 10.dp
private val BORDER = 1.dp

/**
 * Circular button with one icon (48 dp touch target). The [primary] one carries the turquoise-to-violet
 * gradient with a dark icon; the others are translucent glass with a hairline border.
 */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val base = modifier.size(StateAiDimens.TouchTarget)
    val styled = if (primary) {
        base
            .shadow(
                GLOW_ELEVATION,
                CircleShape,
                ambientColor = StateAiColors.AccentGlow,
                spotColor = StateAiColors.AccentGlow,
            )
            .clip(CircleShape)
            .background(StateAiColors.AccentGradient)
    } else {
        base
            .clip(CircleShape)
            .background(StateAiColors.Surface2)
            .border(BorderStroke(BORDER, StateAiColors.Outline), CircleShape)
    }
    Box(styled.clickable(role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (primary) StateAiColors.OnAccent else StateAiColors.Text1,
        )
    }
}
