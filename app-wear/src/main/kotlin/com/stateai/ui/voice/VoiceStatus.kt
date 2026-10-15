package com.stateai.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiDimens

/** The mascot in [expression] with one short line under it (listening, thinking or a failure). */
@Composable
fun VoiceStatus(expression: MascotExpression, text: String, thinking: Boolean = false, compact: Boolean = false) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text,
                        fontSize = if (compact) StateAiDimens.Body else TITLE_SIZE,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    if (thinking) ThinkingDots()
                }
            }
        }
    }
}

private val MASCOT_SIZE = 95.dp
private val TITLE_SIZE = 17.sp
private val RINGS_SIZE = 115.dp
