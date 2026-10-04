package com.stateai.ui.pause

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.pause.BreathingRhythm

private const val MIN_SCALE = 0.4f
private const val MAX_SCALE = 1f
private val CIRCLE_SIZE = 140.dp

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
    PauseScreen(scale = scale.value, inhaling = inhaling, onFinished = onFinished)
}

@Composable
fun PauseScreen(scale: Float, inhaling: Boolean, onFinished: () -> Unit) {
    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(CIRCLE_SIZE).scale(scale)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(stringResource(if (inhaling) R.string.pause_inhale else R.string.pause_exhale))
                CompactButton(onClick = onFinished, label = { Text(stringResource(R.string.pause_finish)) })
            }
        }
    }
}
