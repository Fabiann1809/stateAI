package com.stateai.ui.pause

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.common.clockTicks
import com.stateai.di.appContainer
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.pause.BreathingRhythm
import com.stateai.ui.components.EnergySilhouette
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import kotlin.time.Duration.Companion.seconds

/**
 * Guided breathing. One loop drives both the breath haptic and the circle animation, so they keep
 * the same pace: each cycle starts the vibration, then expands (inhale) and contracts (exhale).
 */
@Composable
fun PauseRoute(onFinished: () -> Unit) {
    val container = appContainer()
    val rhythm = remember { BreathingRhythm() }
    val scale = remember { Animatable(MIN_SCALE) }
    var inhaling by remember { mutableStateOf(true) }
    val energyFlow = remember { container.insights.observeEnergy(clockTicks(container.clock, ENERGY_REFRESH)) }
    val energy by energyFlow.collectAsStateWithLifecycle(initialValue = null)

    DisposableEffect(Unit) {
        container.guidedPause.begin()
        onDispose { container.guidedPause.end() }
    }
    LaunchedEffect(Unit) {
        while (true) {
            container.haptics.sessionPlayer.play(HapticEvent.BREATHE)
            inhaling = true
            scale.animateTo(MAX_SCALE, tween(rhythm.inhale.inWholeMilliseconds.toInt(), easing = LinearOutSlowInEasing))
            inhaling = false
            scale.animateTo(MIN_SCALE, tween(rhythm.exhale.inWholeMilliseconds.toInt(), easing = LinearOutSlowInEasing))
        }
    }
    PauseScreen(scale = scale.value, inhaling = inhaling, energy = energy, onFinished = onFinished)
}

/** No numbers and almost no text: the pause is for not reading. */
@Composable
fun PauseScreen(scale: Float, inhaling: Boolean, energy: EnergyBudget?, onFinished: () -> Unit) {
    ScreenScaffold(timeText = {}) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            energy?.let {
                EnergySilhouette(
                    level = it.level,
                    color = StateAiColors.Normal,
                    height = SILHOUETTE_HEIGHT,
                    modifier = Modifier.alpha(SILHOUETTE_ALPHA),
                )
            }
            BreathingCircle(scale)
            Text(
                text = stringResource(if (inhaling) R.string.pause_inhale else R.string.pause_exhale),
                fontSize = WORD_SIZE,
                fontWeight = FontWeight.Medium,
            )
            BackAction(onFinished, Modifier.align(Alignment.BottomCenter).padding(bottom = BACK_BOTTOM))
        }
    }
}

@Composable
private fun BreathingCircle(scale: Float) {
    Box(
        Modifier
            .size(CIRCLE_SIZE)
            .scale(scale)
            .background(StateAiColors.Accent.copy(alpha = CIRCLE_FILL), CircleShape)
            .border(1.dp, StateAiColors.Accent.copy(alpha = CIRCLE_BORDER), CircleShape),
    )
}

@Composable
private fun BackAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = StateAiDimens.TouchTarget)
            .clickable(onClick = onClick)
            .padding(horizontal = StateAiDimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(StateAiIcons.Back, contentDescription = null, tint = StateAiColors.Text2, modifier = Modifier.size(12.dp))
        Text(stringResource(R.string.pause_finish), fontSize = StateAiDimens.Body, color = StateAiColors.Text2)
    }
}

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 1.6f
private const val CIRCLE_FILL = 0.25f
private const val CIRCLE_BORDER = 0.55f
private const val SILHOUETTE_ALPHA = 0.5f
private val CIRCLE_SIZE = 95.dp
private val SILHOUETTE_HEIGHT = 165.dp
private val WORD_SIZE = 23.sp
private val BACK_BOTTOM = 10.dp
private val ENERGY_REFRESH = 30.seconds
