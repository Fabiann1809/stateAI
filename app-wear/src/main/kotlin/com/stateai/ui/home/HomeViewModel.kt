package com.stateai.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.energy.EnergyBudget
import com.stateai.domain.mascot.MascotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

class HomeViewModel(
    repository: ActivityRepository,
    suggestedActivity: Flow<ActivityId?>,
    focusWindow: Flow<Boolean>,
    energy: Flow<EnergyBudget>,
    private val mascot: MascotRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeActive(),
        suggestedActivity.onStart { emit(null) },
        focusWindow.onStart { emit(false) },
        energy.map<EnergyBudget, EnergyBudget?> { it }.onStart { emit(null) },
        mascot.observeName().map { name -> name?.let(MascotNameState::Known) ?: MascotNameState.NotAsked },
    ) { activities, suggestedId, isFocusWindow, energyBudget, mascotName ->
        val suggested = activities.firstOrNull { it.id == suggestedId }
        HomeUiState(
            activities = listOfNotNull(suggested) + activities.filterNot { it == suggested },
            suggested = suggested,
            canCreateNew = activities.size < ActivityLimits.MAX_ACTIVE,
            isFocusWindow = isFocusWindow,
            energy = energyBudget,
            mascotName = mascotName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

    /** Saves the first-use answer; an empty name means "not now" and is not asked again. */
    fun nameMascot(name: String) {
        viewModelScope.launch { mascot.saveName(name) }
    }
}
