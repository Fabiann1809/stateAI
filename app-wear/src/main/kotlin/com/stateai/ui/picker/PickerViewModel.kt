package com.stateai.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

class PickerViewModel(repository: ActivityRepository) : ViewModel() {
    val uiState: StateFlow<PickerUiState> = repository.observeActive()
        .map { PickerUiState(activities = it, canCreateNew = it.size < ActivityLimits.MAX_ACTIVE) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PickerUiState())
}
