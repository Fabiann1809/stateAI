package com.stateai.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.ui.components.GlassButton
import com.stateai.ui.components.LavenderButton
import com.stateai.ui.components.glowBackground
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/**
 * Confirmation before ending a session from the back gesture: it says how long the session has been
 * going and offers "Seguir" or "Terminar". It covers the session, which keeps running meanwhile.
 */
@Composable
fun EndSessionScreen(elapsed: String, onContinue: () -> Unit, onFinish: () -> Unit) {
    Box(Modifier.fillMaxSize().glowBackground(ambient = false), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
        ) {
            Text(
                text = stringResource(R.string.session_end_title),
                fontSize = TITLE_SIZE,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.session_end_elapsed, elapsed),
                fontSize = StateAiDimens.Label,
                color = StateAiColors.Text2,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
                modifier = Modifier.padding(top = StateAiDimens.SpaceM),
            ) {
                GlassButton(stringResource(R.string.session_end_continue), onContinue)
                LavenderButton(stringResource(R.string.session_stop), onFinish)
            }
        }
    }
}

private val SIDE_PADDING = 30.dp
private val TITLE_SIZE = 17.sp
