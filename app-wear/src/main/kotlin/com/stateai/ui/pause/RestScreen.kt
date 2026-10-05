package com.stateai.ui.pause

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.haptics.HapticEvent
import com.stateai.ui.common.toClockText
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.tabular
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

/**
 * A short break away from the task, with a countdown. It is recorded like the guided pause (state
 * before and after), so it counts for recovery; when time is up the watch pulses once and goes back
 * to the session. "Volver" ends it earlier.
 */
@Composable
fun RestRoute(onFinished: () -> Unit, length: Duration = REST_LENGTH) {
    val container = appContainer()
    var left by remember { mutableStateOf(length) }
    DisposableEffect(Unit) {
        container.guidedPause.begin()
        onDispose { container.guidedPause.end() }
    }
    LaunchedEffect(Unit) {
        while (left > Duration.ZERO) {
            delay(TICK)
            left -= TICK
        }
        container.haptics.sessionPlayer.play(HapticEvent.BLOCK_START)
        onFinished()
    }
    RestScreen(left, onFinished)
}

@Composable
fun RestScreen(left: Duration, onBack: () -> Unit) {
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            ) {
                AnimatedMascot(MascotExpression.HAPPY, size = MASCOT_SIZE)
                Text(stringResource(R.string.rest_title), fontSize = StateAiDimens.Body, color = StateAiColors.Text2)
                Text(
                    text = left.toClockText(),
                    fontSize = TIMER_SIZE,
                    fontWeight = FontWeight.Medium,
                    style = LocalTextStyle.current.tabular(),
                )
                Text(
                    text = stringResource(R.string.rest_tip),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Text2,
                    textAlign = TextAlign.Center,
                )
                CompactButton(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StateAiColors.Surface2,
                        contentColor = StateAiColors.Text1,
                    ),
                    label = { Text(stringResource(R.string.pause_finish)) },
                )
            }
        }
    }
}

private val REST_LENGTH = 5.minutes
private val TICK = 1.seconds
private val MASCOT_SIZE = 56.dp
private val SIDE_PADDING = 28.dp
private val TIMER_SIZE = 34.sp
