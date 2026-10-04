package com.stateai.ui.picker

import com.stateai.domain.activity.Activity

/** What the activity picker shows. */
data class PickerUiState(val activities: List<Activity> = emptyList(), val canCreateNew: Boolean = true)
