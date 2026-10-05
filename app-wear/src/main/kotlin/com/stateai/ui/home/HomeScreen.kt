package com.stateai.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.BuildConfig
import com.stateai.R
import com.stateai.domain.energy.EnergyBudget
import com.stateai.ui.ambient.LocalIsAmbient
import com.stateai.ui.components.EnergyBadge
import com.stateai.ui.components.RoundIconButton
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.components.glowBackground
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

@Composable
fun HomeRoute(onTalk: () -> Unit, onOpenActivities: () -> Unit, onOpenSummary: () -> Unit, onOpenDebug: () -> Unit) {
    val viewModel = homeViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (state.mascotName) {
        MascotNameState.Loading -> return
        MascotNameState.NotAsked -> return MascotNamingScreen(onNamed = viewModel::nameMascot)
        is MascotNameState.Known -> Unit
    }
    HomeScreen(
        state = state,
        actions = HomeActions(
            onTalk = onTalk,
            onOpenActivities = onOpenActivities,
            onOpenSummary = onOpenSummary,
            onOpenDebug = onOpenDebug.takeIf { BuildConfig.DEBUG },
        ),
    )
}

/** What the home screen lets the person do. [onOpenDebug] is null outside debug builds. */
data class HomeActions(
    val onTalk: () -> Unit,
    val onOpenActivities: () -> Unit,
    val onOpenSummary: () -> Unit,
    val onOpenDebug: (() -> Unit)?,
)

/**
 * The main screen: the mascot is the protagonist (tap it and say what you will do), and below it
 * the way to the activity list, today's estimated energy and the day summary.
 */
@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions) {
    val name = (state.mascotName as? MascotNameState.Known)?.name.orEmpty()
    // No system time here: the mascot fills the top of the screen.
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize().glowBackground(LocalIsAmbient.current), contentAlignment = Alignment.TopCenter) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = TOP_PADDING),
            ) {
                TalkingMascot(name, actions.onTalk, actions.onOpenDebug)
                HomeShortcuts(state.energy, actions)
            }
        }
    }
}

/** Tapping the mascot opens the microphone once (never continuous); a long press opens debug tools. */
@Composable
private fun TalkingMascot(name: String, onTalk: () -> Unit, onLongPress: (() -> Unit)?) {
    val hint = if (name.isEmpty()) {
        stringResource(R.string.home_talk_hint)
    } else {
        stringResource(R.string.home_talk_hint_named, name)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(
            onClickLabel = hint,
            role = Role.Button,
            onLongClick = onLongPress,
            onClick = onTalk,
        ),
    ) {
        AnimatedMascot(MascotExpression.REST, size = MASCOT_SIZE, contentDescription = hint)
        Text(
            text = hint,
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text2,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = HINT_PADDING),
        )
    }
}

@Composable
private fun HomeShortcuts(energy: EnergyBudget?, actions: HomeActions) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIconButton(StateAiIcons.List, stringResource(R.string.home_activities), actions.onOpenActivities)
        energy?.let { EnergyBadge(it, color = StateAiColors.Accent, height = ENERGY_HEIGHT, fontSize = ENERGY_TEXT) }
        RoundIconButton(StateAiIcons.Bars, stringResource(R.string.summary_open), actions.onOpenSummary)
    }
}

private val MASCOT_SIZE = 116.dp
private val TOP_PADDING = 10.dp
private val HINT_PADDING = 30.dp
private val ENERGY_HEIGHT = 28.dp
private val ENERGY_TEXT = 15.sp
