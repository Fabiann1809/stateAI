package com.stateai.ui.home

import com.stateai.domain.activity.Activity
import com.stateai.domain.energy.EnergyBudget

/** What the home screen shows: the activity list under the mascot. */
data class HomeUiState(
    /** Suggested activity first (if any), then the rest by recent use. */
    val activities: List<Activity> = emptyList(),
    val suggested: Activity? = null,
    val canCreateNew: Boolean = true,
    /** Now is a learned focus window (enough history and a clearly better hour). */
    val isFocusWindow: Boolean = false,
    /** Today's estimated energy, null until the first value arrives. */
    val energy: EnergyBudget? = null,
    val mascotName: MascotNameState = MascotNameState.Loading,
)

/** Whether the mascot still has to be named (asked once, on first use). */
sealed interface MascotNameState {
    data object Loading : MascotNameState

    data object NotAsked : MascotNameState

    /** [name] is empty when the person chose not to name it. */
    data class Known(val name: String) : MascotNameState
}
