package com.stateai.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.ui.mascot.Mascot
import com.stateai.ui.mascot.MascotExpression
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
            item { Mascot(MascotExpression.REST, size = MASCOT_SIZE) }
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
                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(accept, fontWeight = FontWeight.Medium) },
                )
            }
            item {
                DeclineButton(decline) { onAnswer(false) }
            }
        }
    }
}

@Composable
private fun DeclineButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = StateAiColors.Surface2,
            contentColor = StateAiColors.Text1,
        ),
        label = { Text(label) },
    )
}

private val MASCOT_SIZE = 56.dp
