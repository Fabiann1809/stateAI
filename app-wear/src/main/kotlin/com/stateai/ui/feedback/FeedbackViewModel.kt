package com.stateai.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stateai.domain.learning.RecordFeedback
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.SegmentId
import kotlinx.coroutines.launch

class FeedbackViewModel(private val segmentId: SegmentId, private val recordFeedback: RecordFeedback) : ViewModel() {
    fun answer(feedback: Feedback, onSaved: () -> Unit) {
        viewModelScope.launch {
            recordFeedback(segmentId, feedback)
            onSaved()
        }
    }
}
