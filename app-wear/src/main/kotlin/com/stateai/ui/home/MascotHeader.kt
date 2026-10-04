package com.stateai.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/**
 * Top of the home screen: the mascot (with its name, if it has one) and the voice hint. Tapping it
 * opens the microphone once (never continuous listening). In debug builds a long press opens the
 * debug tools.
 */
@Composable
fun MascotHeader(name: String, onTalk: () -> Unit, onLongPress: (() -> Unit)?) {
    val hint = stringResource(R.string.home_talk_hint)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClickLabel = hint, role = Role.Button, onLongClick = onLongPress, onClick = onTalk),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (name.isNotEmpty()) {
            Text(name, fontSize = StateAiDimens.Label, fontWeight = FontWeight.Medium, color = StateAiColors.Accent)
        }
        AnimatedMascot(MascotExpression.REST, size = MASCOT_SIZE, contentDescription = hint)
        Text(hint, fontSize = StateAiDimens.Body, color = StateAiColors.Text2, textAlign = TextAlign.Center)
    }
}

private val MASCOT_SIZE = 96.dp
