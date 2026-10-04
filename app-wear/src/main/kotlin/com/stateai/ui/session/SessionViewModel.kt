package com.stateai.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import com.stateai.ui.common.clockTicks
import java.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

class SessionViewModel(
    activityId: ActivityId,
    startSession: StartSession,
    private val tracker: SessionTracker,
    clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<SessionUiState> = combine(tracker.activeSession, clockTicks(clock)) { session, now ->
        SessionUiState(activity = session?.activity, progress = session?.progressAt(now))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SessionUiState())

    init {
        viewModelScope.launch { startSession(activityId) }
    }

    fun stop() {
        tracker.stop()
    }
}
