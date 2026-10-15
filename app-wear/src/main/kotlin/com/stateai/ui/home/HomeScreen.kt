package com.stateai.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.BuildConfig
import com.stateai.R
import com.stateai.domain.energy.EnergyBudget
import com.stateai.ui.components.EnergyBadge
import com.stateai.ui.components.RoundIconButton
import com.stateai.ui.components.StateAiIcons
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
 * The main screen: today's estimated energy on top, the mascot as the protagonist (tap it and say what
 * you will do), and below it the way to the activity list and to the day summary.
 */
@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions) {
    val name = (state.mascotName as? MascotNameState.Known)?.name.orEmpty()
    // No system time here: the energy pill and the mascot fill the top of the screen.
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceXs),
                modifier = Modifier.padding(top = TOP_PADDING),
            ) {
                state.energy?.let { EnergyPill(it) }
                TalkingMascot(name, actions.onTalk, actions.onOpenDebug)
                HomeShortcuts(actions)
            }
        }
    }
}

/** Today's energy as a small translucent pill: the silhouette and the percentage. */
@Composable
private fun EnergyPill(energy: EnergyBudget) {
    EnergyBadge(
        energy = energy,
        color = StateAiColors.Accent,
        height = PILL_SILHOUETTE,
        fontSize = PILL_TEXT,
        modifier = Modifier
            .background(StateAiColors.Surface2, CircleShape)
            .border(BorderStroke(1.dp, StateAiColors.Outline), CircleShape)
            .padding(horizontal = PILL_PADDING, vertical = PILL_VERTICAL_PADDING),
    )
}

/** Tapping the mascot opens the microphone once (never continuous); a long press opens debug tools. */
@Composable
private fun TalkingMascot(name: String, onTalk: () -> Unit, onLongPress: (() -> Unit)?) {
    val hint = talkHint(name)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(
            onClickLabel = hint.text,
            role = Role.Button,
            onLongClick = onLongPress,
            onClick = onTalk,
        ),
    ) {
        AnimatedMascot(MascotExpression.REST, size = MASCOT_SIZE, contentDescription = hint.text)
        Text(
            text = hint,
            fontSize = HINT_SIZE,
            color = StateAiColors.Text2,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = HINT_PADDING),
        )
    }
}

/** "Toca a Lumi para hablar", with the name of the mascot in bold. */
@Composable
private fun talkHint(name: String) = if (name.isEmpty()) {
    buildAnnotatedString { append(stringResource(R.string.home_talk_hint)) }
} else {
    val full = stringResource(R.string.home_talk_hint_named, name)
    buildAnnotatedString {
        val start = full.indexOf(name)
        append(full.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = StateAiColors.Text1)) { append(name) }
        append(full.substring(start + name.length))
    }
}

@Composable
private fun HomeShortcuts(actions: HomeActions) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIconButton(
            StateAiIcons.List,
            stringResource(R.string.home_activities),
            actions.onOpenActivities,
            primary = true,
        )
        RoundIconButton(StateAiIcons.Bars, stringResource(R.string.summary_open), actions.onOpenSummary)
    }
}

private val MASCOT_SIZE = 96.dp
private val TOP_PADDING = 10.dp
private val HINT_PADDING = 30.dp
private val HINT_SIZE = 13.sp
private val PILL_SILHOUETTE = 17.dp
private val PILL_TEXT = 13.sp
private val PILL_PADDING = 10.dp
private val PILL_VERTICAL_PADDING = 3.dp
