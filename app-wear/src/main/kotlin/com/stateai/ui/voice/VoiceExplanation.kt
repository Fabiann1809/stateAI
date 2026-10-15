package com.stateai.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.ui.components.GradientButton
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.components.TextAction
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/**
 * A short explanation before something that needs the person's agreement (microphone permission,
 * system recognizer): the mascot, a title, why, and two answers. Declining always leads back to
 * the activity list.
 */
@Composable
fun VoiceExplanation(title: String, body: String, accept: String, decline: String, onAnswer: (Boolean) -> Unit) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    ScreenScaffold(scrollState = listState, timeText = {}) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
        ) {
            item { MicTile() }
            item {
                Text(
                    title,
                    fontSize = StateAiDimens.Title,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Text(body, fontSize = StateAiDimens.Body, color = StateAiColors.Text2, textAlign = TextAlign.Center)
            }
            item {
                GradientButton(accept, onClick = { onAnswer(true) }, modifier = Modifier.fillMaxWidth())
            }
            item { TextAction(decline, onClick = { onAnswer(false) }) }
        }
    }
}

/** The microphone on a soft turquoise disc. */
@Composable
private fun MicTile() {
    Box(
        Modifier.size(TILE_SIZE).background(StateAiColors.AccentSoft, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            StateAiIcons.Mic,
            contentDescription = null,
            tint = StateAiColors.Accent,
            modifier = Modifier.size(ICON_SIZE),
        )
    }
}

private val TILE_SIZE = 40.dp
private val ICON_SIZE = 20.dp
