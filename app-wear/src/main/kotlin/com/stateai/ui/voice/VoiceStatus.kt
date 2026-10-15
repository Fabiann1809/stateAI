package com.stateai.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiDimens

/** The mascot in [expression] with one short line under it (listening, thinking or a failure). */
@Composable
fun VoiceStatus(expression: MascotExpression, text: String) {
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
                modifier = Modifier.padding(horizontal = 32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (expression == MascotExpression.LISTENING) ListeningRings(RINGS_SIZE)
                    AnimatedMascot(expression, size = MASCOT_SIZE, contentDescription = text)
                }
                Text(
                    text,
                    fontSize = StateAiDimens.Title,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private val MASCOT_SIZE = 96.dp
private val RINGS_SIZE = 190.dp
