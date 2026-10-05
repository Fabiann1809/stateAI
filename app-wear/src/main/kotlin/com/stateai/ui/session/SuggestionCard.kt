package com.stateai.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.session.PauseKind
import com.stateai.domain.session.Suggestion
import com.stateai.domain.session.SuggestionReason
import com.stateai.ui.common.toHoursMinutesText
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/**
 * Shown over the session when the watch vibrates to suggest a pause: the mascot, why, and two
 * answers. It only suggests: "Ahora no" (or ignoring it) keeps the session as it is.
 */
@Composable
fun SuggestionCard(suggestion: Suggestion, onAccept: (PauseKind) -> Unit, onDismiss: () -> Unit) {
    val pause = suggestion.reason.pause
    val acceptLabel = if (pause == PauseKind.BREATHE) R.string.suggestion_breathe else R.string.suggestion_rest
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
        ) {
            AnimatedMascot(MascotExpression.REST, size = MASCOT_SIZE)
            Text(
                text = message(suggestion),
                fontSize = StateAiDimens.Body,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 3,
            )
            Button(
                onClick = { onAccept(pause) },
                label = { Text(stringResource(acceptLabel), fontWeight = FontWeight.Medium) },
            )
            CompactButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = StateAiColors.Surface2),
                label = { Text(stringResource(R.string.suggestion_dismiss), color = StateAiColors.Text2) },
            )
        }
    }
}

@Composable
private fun message(suggestion: Suggestion): String = when (suggestion.reason) {
    SuggestionReason.OVERLOAD -> stringResource(R.string.suggestion_overload)
    SuggestionReason.RESTLESS -> stringResource(R.string.suggestion_restless)
    SuggestionReason.LEFT_FOCUS -> stringResource(R.string.suggestion_left_focus)
    SuggestionReason.BLOCK_DONE -> stringResource(R.string.suggestion_block_done, suggestion.elapsed.inWholeMinutes)
    SuggestionReason.LONG_SESSION ->
        stringResource(R.string.suggestion_long_session, suggestion.elapsed.toHoursMinutesText())
}

private val MASCOT_SIZE = 52.dp
private val SIDE_PADDING = 24.dp
