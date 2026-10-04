package com.stateai.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** Circular secondary button with one icon (48 dp touch target). */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = StateAiColors.Text1,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(StateAiDimens.TouchTarget),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = StateAiColors.Surface2,
            contentColor = tint,
        ),
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}
