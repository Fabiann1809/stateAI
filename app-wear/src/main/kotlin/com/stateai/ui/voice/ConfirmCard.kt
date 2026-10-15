package com.stateai.ui.voice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.activity.ActivityCategory
import com.stateai.ui.common.labelRes
import com.stateai.ui.components.GlassButton
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
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Countdown(secondsLeft)
                Row(horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS)) {
                    GlassButton(stringResource(R.string.voice_change), onChange)
                    GlassButton(stringResource(R.string.voice_cancel), onCancel)
                }
            }
        }
    }
}

/** The seconds left, in a soft capsule with a small ring that empties as they pass. */
@Composable
private fun Countdown(secondsLeft: Int) {
    val fraction = secondsLeft.toFloat() / VoiceViewModel.COUNTDOWN_SECONDS
    Row(
        modifier = Modifier
            .background(StateAiColors.AccentSoft, CircleShape)
            .padding(start = RING_PADDING, end = TEXT_PADDING, top = RING_PADDING, bottom = RING_PADDING),
        horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(RING_SIZE)) {
            val stroke = RING_STROKE.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(StateAiColors.Surface, 0f, FULL_TURN, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                color = StateAiColors.Accent,
                startAngle = -90f,
                sweepAngle = FULL_TURN * fraction,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text(
            text = stringResource(R.string.voice_confirm_countdown, secondsLeft),
            fontSize = StateAiDimens.Label,
            fontWeight = FontWeight.Medium,
            color = StateAiColors.Accent,
        )
    }
}

private const val FULL_TURN = 360f
private val MASCOT_SIZE = 52.dp
private val ACTIVITY_SIZE = 17.sp
private val RING_SIZE = 18.dp
private val RING_STROKE = 2.dp
private val RING_PADDING = 3.dp
private val TEXT_PADDING = 10.dp
