package com.stateai.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.common.clockTicks
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.session.EndSession
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.session.SessionSuggestions
import com.stateai.domain.session.SessionTracker
import com.stateai.domain.session.StartSession
import java.time.Clock
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** An unanswered suggestion disappears after this; it only suggests, it never insists. */
private val SUGGESTION_SHOWN_FOR: Duration = Duration.ofMinutes(5)

/** What the session screen needs from the rest of the app. */
class SessionDependencies(
    val startSession: StartSession,
    val endSession: EndSession,
    val tracker: SessionTracker,
    val status: StateFlow<MonitorStatus>,
    val energy: Flow<EnergyBudget>,
    val clock: Clock,
    val suggestions: SessionSuggestions = SessionSuggestions(),
)

class SessionViewModel(activityId: ActivityId, private val dependencies: SessionDependencies) : ViewModel() {
    val uiState: StateFlow<SessionUiState> = combine(
        dependencies.tracker.activeSession,
        dependencies.status,
        dependencies.energy.onStart<EnergyBudget?> { emit(null) },
        clockTicks(dependencies.clock),
        dependencies.suggestions.current,
    ) { session, monitorStatus, budget, now, suggestion ->
        SessionUiState(
            activity = session?.activity,
            progress = session?.progressAt(now),
            status = monitorStatus,
            energy = budget,
            suggestion = suggestion?.takeIf { Duration.between(it.at, now) < SUGGESTION_SHOWN_FOR },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SessionUiState())

    init {
        viewModelScope.launch { dependencies.startSession(activityId) }
    }

    /** "Ahora no", or the suggestion was taken: either way it leaves the screen. */
    fun dismissSuggestion() {
        dependencies.suggestions.dismiss()
    }

    /** Ends the session and reports the closed segment, if any. */
    fun stop(onEnded: (SegmentId?) -> Unit) {
        viewModelScope.launch { onEnded(dependencies.endSession()?.id) }
    }
}
