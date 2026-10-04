package com.stateai.ui.sessionsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRepository
import com.stateai.domain.summary.SessionReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads the just-finished segment once and turns it into a [SessionReport]. */
class SessionSummaryViewModel(segmentId: SegmentId, segments: SegmentRepository) : ViewModel() {
    private val current = MutableStateFlow<SessionReport?>(null)
    val report: StateFlow<SessionReport?> = current.asStateFlow()

    init {
        viewModelScope.launch { current.value = segments.find(segmentId)?.let { SessionReport.of(it) } }
    }
}
