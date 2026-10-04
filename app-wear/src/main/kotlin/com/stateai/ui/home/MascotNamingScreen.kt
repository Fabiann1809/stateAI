package com.stateai.ui.home

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
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.ui.common.rememberTextInput
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** First use only: the person can give the mascot a name, or skip it ("Ahora no"). */
@Composable
fun MascotNamingScreen(onNamed: (String) -> Unit) {
    val askName = rememberTextInput(label = stringResource(R.string.mascot_name_label), onText = onNamed)
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                AnimatedMascot(MascotExpression.HAPPY, size = MASCOT_SIZE)
                Text(
                    text = stringResource(R.string.mascot_name_question),
                    fontSize = StateAiDimens.Body,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = askName,
                    label = { Text(stringResource(R.string.mascot_name_action), fontWeight = FontWeight.Medium) },
                )
                CompactButton(
                    onClick = { onNamed("") },
                    colors = ButtonDefaults.buttonColors(containerColor = StateAiColors.Surface2),
                    label = { Text(stringResource(R.string.mascot_name_skip), color = StateAiColors.Text2) },
                )
            }
        }
    }
}

private val MASCOT_SIZE = 64.dp
