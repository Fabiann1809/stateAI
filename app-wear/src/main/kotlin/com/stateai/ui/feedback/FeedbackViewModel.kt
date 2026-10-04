package com.stateai.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRepository
import kotlinx.coroutines.launch

class FeedbackViewModel(private val segmentId: SegmentId, private val segments: SegmentRepository) : ViewModel() {
    fun answer(feedback: Feedback, onSaved: () -> Unit) {
        viewModelScope.launch {
            segments.setFeedback(segmentId, feedback)
            onSaved()
        }
    }
}
