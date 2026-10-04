package com.stateai.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.common.clockTicks
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.session.EndSession
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** What the session screen needs from the rest of the app. */
class SessionDependencies(
    val startSession: StartSession,
    val endSession: EndSession,
    val tracker: SessionTracker,
    val status: StateFlow<MonitorStatus>,
    val energy: Flow<EnergyBudget>,
    val clock: Clock,
)

class SessionViewModel(activityId: ActivityId, private val dependencies: SessionDependencies) : ViewModel() {
    val uiState: StateFlow<SessionUiState> = combine(
        dependencies.tracker.activeSession,
        dependencies.status,
        dependencies.energy.onStart<EnergyBudget?> { emit(null) },
        clockTicks(dependencies.clock),
    ) { session, monitorStatus, budget, now ->
        SessionUiState(
            activity = session?.activity,
            progress = session?.progressAt(now),
            status = monitorStatus,
            energy = budget,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SessionUiState())

    init {
        viewModelScope.launch { dependencies.startSession(activityId) }
    }

    /** Ends the session and reports the closed segment, if any. */
    fun stop(onEnded: (SegmentId?) -> Unit) {
        viewModelScope.launch { onEnded(dependencies.endSession()?.id) }
    }
}
