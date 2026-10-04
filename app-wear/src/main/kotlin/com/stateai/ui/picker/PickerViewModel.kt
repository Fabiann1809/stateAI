package com.stateai.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.energy.EnergyBudget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

class PickerViewModel(
    repository: ActivityRepository,
    suggestedActivity: Flow<ActivityId?>,
    focusWindow: Flow<Boolean>,
    energy: Flow<EnergyBudget>,
) : ViewModel() {
    val uiState: StateFlow<PickerUiState> = combine(
        repository.observeActive(),
        suggestedActivity.onStart { emit(null) },
        focusWindow.onStart { emit(false) },
        energy.map<EnergyBudget, EnergyBudget?> { it }.onStart { emit(null) },
    ) { activities, suggestedId, isFocusWindow, energyBudget ->
        val suggested = activities.firstOrNull { it.id == suggestedId }
        PickerUiState(
            activities = listOfNotNull(suggested) + activities.filterNot { it == suggested },
            suggested = suggested,
            canCreateNew = activities.size < ActivityLimits.MAX_ACTIVE,
            isFocusWindow = isFocusWindow,
            energy = energyBudget,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PickerUiState())
}
