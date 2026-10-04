package com.stateai.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.summary.DaySummary
import com.stateai.domain.summary.ObserveDaySummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

class SummaryViewModel(observeDaySummary: ObserveDaySummary) : ViewModel() {
    val summary: StateFlow<DaySummary?> = observeDaySummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)
}
