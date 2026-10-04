package com.stateai.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val colorScheme = ColorScheme(
    primary = StateAiColors.Accent,
    onPrimary = StateAiColors.OnAccent,
    primaryContainer = StateAiColors.Surface2,
    onPrimaryContainer = StateAiColors.Text1,
    surfaceContainerLow = StateAiColors.Surface,
    surfaceContainer = StateAiColors.Surface2,
    surfaceContainerHigh = StateAiColors.Surface2,
    onSurface = StateAiColors.Text1,
    onSurfaceVariant = StateAiColors.Text2,
    outline = StateAiColors.Outline,
    outlineVariant = StateAiColors.Outline,
    background = StateAiColors.Background,
    onBackground = StateAiColors.Text1,
)

/** Wear Material 3 themed with the stateAI tokens. */
@Composable
fun StateAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/** Same style with tabular figures, so timers and percentages do not jitter. */
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
