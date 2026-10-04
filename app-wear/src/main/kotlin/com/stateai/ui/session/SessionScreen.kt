package com.stateai.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.common.clockTicks
import com.stateai.di.appContainer
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.session.SessionProgress
import com.stateai.domain.state.DisplayState
import com.stateai.ui.ambient.LocalIsAmbient
import com.stateai.ui.common.RequestSessionPermissions
import com.stateai.ui.common.title
import com.stateai.ui.common.toClockText
import com.stateai.ui.components.EnergyBadge
import com.stateai.ui.components.PageIndicator
import com.stateai.ui.components.ProgressRing
import com.stateai.ui.components.RoundIconButton
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.color
import com.stateai.ui.theme.tabular
import kotlin.time.Duration.Companion.seconds

@Composable
fun SessionRoute(activityId: ActivityId, onPause: () -> Unit, onStopped: (SegmentId?) -> Unit) {
    val container = appContainer()
    val viewModel: SessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val dependencies = SessionDependencies(
                    startSession = container.startSession,
                    endSession = container.endSession,
                    tracker = container.sessionTracker,
                    status = container.sessionMonitor.status,
                    energy = container.insights.observeEnergy(clockTicks(container.clock, ENERGY_REFRESH)),
                    clock = container.clock,
                )
                SessionViewModel(activityId, dependencies)
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RequestSessionPermissions()
    val actions = SessionActions(onPause = onPause, onStop = { viewModel.stop(onStopped) })
    SessionScreen(state = state, isAmbient = LocalIsAmbient.current, actions = actions)
}

/** User actions available during a session. */
data class SessionActions(val onPause: () -> Unit, val onStop: () -> Unit)

/** What the session shows right now: the display state, if there is an estimate. */
private val SessionUiState.displayState: DisplayState?
    get() = (status as? MonitorStatus.Estimating)?.let { DisplayState.of(it.estimate, energy?.isLow == true) }

@Composable
fun SessionScreen(state: SessionUiState, isAmbient: Boolean, actions: SessionActions) {
    val progress = state.progress ?: return
    // No system time here: the session timer is the protagonist and the title sits at the top.
    ScreenScaffold(timeText = {}) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isAmbient) {
                AmbientSessionContent(progress, state.displayState, state.energy)
            } else {
                SessionPages(state, progress, actions)
            }
        }
    }
}

/** Swipe left for the energy page; the session page keeps the progress ring. */
@Composable
private fun SessionPages(state: SessionUiState, progress: SessionProgress, actions: SessionActions) {
    val energy = state.energy
    val pagerState = rememberPagerState(pageCount = { if (energy == null) 1 else 2 })
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (page == 0 || energy == null) {
                    ProgressRing(progress.fraction)
                    ActiveSessionContent(state, progress, actions)
                } else {
                    SessionEnergyPage(energy, state.displayState)
                }
            }
        }
        if (pagerState.pageCount > 1) {
            PageIndicator(
                count = pagerState.pageCount,
                current = pagerState.currentPage,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = INDICATOR_BOTTOM),
            )
        }
    }
}

@Composable
private fun ActiveSessionContent(state: SessionUiState, progress: SessionProgress, actions: SessionActions) {
    val displayState = state.displayState
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(state.activity?.title().orEmpty(), fontSize = StateAiDimens.Label, color = StateAiColors.Text3)
        Text(
            text = progress.elapsed.toClockText(),
            fontSize = StateAiDimens.Display,
            fontWeight = FontWeight.Medium,
            style = LocalTextStyle.current.tabular(),
        )
        Text(
            text = stringResource(R.string.session_target, progress.target.inWholeMinutes),
            fontSize = StateAiDimens.Label,
            color = StateAiColors.Text3,
        )
        Spacer(Modifier.height(StateAiDimens.SpaceXs))
        SessionStatusRow(state.status, displayState)
        state.energy?.let { energy ->
            EnergyBadge(energy, color = displayState?.color() ?: StateAiColors.Accent)
            if (energy.isLow) {
                Text(
                    text = stringResource(R.string.energy_low_short),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Text2,
                )
            }
        }
        Spacer(Modifier.height(StateAiDimens.SpaceXs))
        Row(horizontalArrangement = Arrangement.spacedBy(BUTTON_SPACING)) {
            RoundIconButton(StateAiIcons.Pause, stringResource(R.string.session_pause), actions.onPause)
            RoundIconButton(
                StateAiIcons.Check,
                stringResource(R.string.session_stop),
                actions.onStop,
                tint = StateAiColors.Accent,
            )
        }
    }
}

@Composable
private fun AmbientSessionContent(progress: SessionProgress, displayState: DisplayState?, energy: EnergyBudget?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceS),
    ) {
        Text(
            text = progress.elapsed.toClockText(),
            fontSize = AMBIENT_TIMER,
            color = StateAiColors.Ambient,
            style = LocalTextStyle.current.tabular(),
        )
        displayState?.let { AmbientStateRow(it) }
        energy?.let { EnergyBadge(it, color = StateAiColors.Ambient, ambient = true) }
    }
}

private val BUTTON_SPACING = 11.dp
private val INDICATOR_BOTTOM = 8.dp
private val AMBIENT_TIMER = 48.sp
private val ENERGY_REFRESH = 30.seconds
