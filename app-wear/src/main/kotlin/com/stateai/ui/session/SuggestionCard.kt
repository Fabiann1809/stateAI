package com.stateai.ui.session

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.session.PauseKind
import com.stateai.domain.session.Suggestion
import com.stateai.domain.session.SuggestionReason
import com.stateai.ui.common.toHoursMinutesText
import com.stateai.ui.components.GlassButton
import com.stateai.ui.components.GradientButton
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
    val pop = remember { Animatable(POP_START) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
    Box(Modifier.fillMaxSize().reasonGlow(glow(suggestion.reason)), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
        ) {
            AnimatedMascot(
                MascotExpression.REST,
                size = MASCOT_SIZE,
                modifier = Modifier.graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                },
            )
            Text(
                text = stringResource(title(suggestion.reason)),
                fontSize = StateAiDimens.Label,
                fontWeight = FontWeight.SemiBold,
                color = color(suggestion.reason),
                textAlign = TextAlign.Center,
            )
            Text(
                text = message(suggestion),
                fontSize = StateAiDimens.Body,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 4,
            )
            GradientButton(stringResource(acceptLabel), onClick = { onAccept(pause) })
            GlassButton(stringResource(R.string.suggestion_dismiss), onDismiss)
        }
    }
}

@StringRes
private fun title(reason: SuggestionReason): Int = when (reason) {
    SuggestionReason.OVERLOAD -> R.string.suggestion_overload_title
    SuggestionReason.RESTLESS -> R.string.suggestion_restless_title
    SuggestionReason.LEFT_FOCUS -> R.string.suggestion_left_focus_title
    SuggestionReason.BLOCK_DONE -> R.string.suggestion_block_done_title
    SuggestionReason.LONG_SESSION -> R.string.suggestion_long_session_title
}

/** Each reason has the color of what it is about: overload, restlessness, focus, the block or the long session. */
private fun color(reason: SuggestionReason): Color = when (reason) {
    SuggestionReason.OVERLOAD -> StateAiColors.Overloaded
    SuggestionReason.RESTLESS -> StateAiColors.Normal
    SuggestionReason.LEFT_FOCUS -> StateAiColors.Focused
    SuggestionReason.BLOCK_DONE -> StateAiColors.Accent
    SuggestionReason.LONG_SESSION -> StateAiColors.Ice
}

/** A soft radial glow near the top of the card. */
private fun Modifier.reasonGlow(color: Color): Modifier = drawBehind {
    val radius = size.width * GLOW_RADIUS
    val center = Offset(size.width / 2, size.height * GLOW_CENTER_Y)
    drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), center, radius), radius, center)
}

/** The glow behind the card takes the color of the reason; the block and the long session use the accent. */
private fun glow(reason: SuggestionReason): Color = when (reason) {
    SuggestionReason.OVERLOAD -> StateAiColors.Overloaded.copy(alpha = STRONG_GLOW)
    SuggestionReason.RESTLESS -> StateAiColors.Normal.copy(alpha = STRONG_GLOW)
    SuggestionReason.LEFT_FOCUS -> StateAiColors.Focused.copy(alpha = SOFT_GLOW)
    SuggestionReason.BLOCK_DONE, SuggestionReason.LONG_SESSION -> StateAiColors.AccentGlow
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

private const val POP_START = 0.5f
private const val GLOW_RADIUS = 0.5f
private const val GLOW_CENTER_Y = 0.22f
private const val STRONG_GLOW = 0.38f
private const val SOFT_GLOW = 0.32f
private val MASCOT_SIZE = 52.dp
private val SIDE_PADDING = 24.dp
