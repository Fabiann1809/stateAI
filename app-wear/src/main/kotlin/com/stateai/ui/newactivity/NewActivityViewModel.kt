package com.stateai.ui.newactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.activity.CreateActivityResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Two-step creation: pick a category, then optionally name the activity. [suggestedName] is what a
 * spoken request understood, offered as a one-tap name.
 */
data class NewActivityUiState(
    val category: ActivityCategory? = null,
    val suggestedName: String? = null,
    val readyActivityId: ActivityId? = null,
    val limitReached: Boolean = false,
)

/** What a voice request already understood, to start "Nueva actividad" from it. */
data class NewActivityPrefill(val category: ActivityCategory? = null, val name: String? = null)

class NewActivityViewModel(
    private val createActivity: CreateActivity,
    prefill: NewActivityPrefill = NewActivityPrefill(),
) : ViewModel() {
    private val state = MutableStateFlow(NewActivityUiState(category = prefill.category, suggestedName = prefill.name))
    val uiState: StateFlow<NewActivityUiState> = state.asStateFlow()

    fun selectCategory(category: ActivityCategory) {
        state.update { it.copy(category = category) }
    }

    fun create(name: String?) {
        val category = state.value.category ?: return
        viewModelScope.launch {
            when (val result = createActivity(category, name)) {
                is CreateActivityResult.Created -> state.update { it.copy(readyActivityId = result.activity.id) }
                is CreateActivityResult.Existing -> state.update { it.copy(readyActivityId = result.activity.id) }
                CreateActivityResult.LimitReached -> state.update { it.copy(limitReached = true) }
            }
        }
    }
}
