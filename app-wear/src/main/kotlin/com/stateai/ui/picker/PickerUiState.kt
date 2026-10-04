package com.stateai.ui.picker

import com.stateai.domain.activity.Activity

/** What the activity picker shows. */
data class PickerUiState(
    /** Suggested activity first (if any), then the rest by recent use. */
    val activities: List<Activity> = emptyList(),
    val suggested: Activity? = null,
    val canCreateNew: Boolean = true,
    /** Now is a learned focus window (enough history and a clearly better hour). */
    val isFocusWindow: Boolean = false,
)
