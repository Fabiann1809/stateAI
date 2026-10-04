package com.stateai.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.session.SessionProgress
import com.stateai.ui.ambient.LocalIsAmbient
import com.stateai.ui.common.RequestSessionPermissions
import com.stateai.ui.common.title
import com.stateai.ui.common.toClockText

@Composable
fun SessionRoute(activityId: ActivityId, onPause: () -> Unit, onStopped: (SegmentId?) -> Unit) {
    val container = appContainer()
    val viewModel: SessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SessionViewModel(
                    activityId,
                    container.startSession,
                    container.endSession,
                    container.sessionTracker,
                    container.sessionMonitor.status,
                    container.clock,
                )
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

@Composable
fun SessionScreen(state: SessionUiState, isAmbient: Boolean, actions: SessionActions) {
    val progress = state.progress ?: return
    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isAmbient) {
                Text(text = progress.elapsed.toClockText(), style = MaterialTheme.typography.displaySmall)
            } else {
                CircularProgressIndicator(progress = { progress.fraction }, modifier = Modifier.fillMaxSize())
                ActiveSessionContent(
                    title = state.activity?.title().orEmpty(),
                    progress = progress,
                    status = state.status,
                    actions = actions,
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionContent(
    title: String,
    progress: SessionProgress,
    status: MonitorStatus,
    actions: SessionActions,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = title, style = MaterialTheme.typography.labelMedium)
        Text(text = progress.elapsed.toClockText(), style = MaterialTheme.typography.displayMedium)
        Text(
            text = stringResource(R.string.session_target, progress.target.inWholeMinutes),
            style = MaterialTheme.typography.bodySmall,
        )
        SessionStatusRow(status)
        Row(horizontalArrangement = Arrangement.spacedBy(BUTTON_SPACING)) {
            CompactButton(onClick = actions.onPause, label = { Text(stringResource(R.string.session_pause)) })
            CompactButton(onClick = actions.onStop, label = { Text(stringResource(R.string.session_stop)) })
        }
    }
}

private val BUTTON_SPACING = 4.dp
