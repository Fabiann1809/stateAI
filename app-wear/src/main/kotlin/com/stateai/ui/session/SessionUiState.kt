package com.stateai.ui.session

import com.stateai.domain.activity.Activity
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.session.MonitorStatus
import com.stateai.domain.session.SessionProgress
import com.stateai.domain.session.Suggestion

/** What the session screen shows; null fields mean the session is still starting. */
data class SessionUiState(
    val activity: Activity? = null,
    val progress: SessionProgress? = null,
    val status: MonitorStatus = MonitorStatus.Waiting,
    val energy: EnergyBudget? = null,
    /** A pause suggestion to show over the session, if a recent one has not been answered. */
    val suggestion: Suggestion? = null,
)
