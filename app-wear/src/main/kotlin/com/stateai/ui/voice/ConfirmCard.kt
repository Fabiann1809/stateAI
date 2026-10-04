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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.activity.ActivityCategory
import com.stateai.ui.common.labelRes
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** "¿Empezamos <Categoría: nombre>?" with the seconds left before it starts on its own. */
@Composable
fun ConfirmCard(
    category: ActivityCategory,
    name: String?,
    secondsLeft: Int,
    onChange: () -> Unit,
    onCancel: () -> Unit,
) {
    val categoryLabel = stringResource(category.labelRes())
    val activity = name?.let { stringResource(R.string.voice_activity_label, categoryLabel, it) } ?: categoryLabel
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
                modifier = Modifier.padding(horizontal = 26.dp),
            ) {
                AnimatedMascot(MascotExpression.HAPPY, size = MASCOT_SIZE)
                Text(
                    stringResource(R.string.voice_confirm_title),
                    fontSize = StateAiDimens.Body,
                    color = StateAiColors.Text2,
                )
                Text(
                    text = activity,
                    fontSize = ACTIVITY_SIZE,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Text(
                    text = stringResource(R.string.voice_confirm_countdown, secondsLeft),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Accent,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS)) {
                    SecondaryButton(stringResource(R.string.voice_change), onChange)
                    SecondaryButton(stringResource(R.string.voice_cancel), onCancel)
                }
            }
        }
    }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit) {
    CompactButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = StateAiColors.Surface2,
            contentColor = StateAiColors.Text1,
        ),
        label = { Text(label) },
    )
}

private val MASCOT_SIZE = 52.dp
private val ACTIVITY_SIZE = 16.sp
