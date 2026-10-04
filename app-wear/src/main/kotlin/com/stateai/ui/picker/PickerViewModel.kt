package com.stateai.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

class PickerViewModel(repository: ActivityRepository, focusWindow: Flow<Boolean>) : ViewModel() {
    val uiState: StateFlow<PickerUiState> = combine(
        repository.observeActive(),
        focusWindow.onStart { emit(false) },
    ) { activities, isFocusWindow ->
        PickerUiState(
            activities = activities,
            canCreateNew = activities.size < ActivityLimits.MAX_ACTIVE,
            isFocusWindow = isFocusWindow,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PickerUiState())
}
