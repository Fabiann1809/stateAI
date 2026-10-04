package com.stateai.domain.testing

import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory segment storage for domain tests. */
class FakeSegmentRepository : SegmentRepository {
    private val segments = MutableStateFlow<List<Segment>>(emptyList())

    override suspend fun save(segment: Segment) {
        segments.value = segments.value.filterNot { it.id == segment.id } + segment
    }

    override suspend fun setFeedback(id: SegmentId, feedback: Feedback) {
        segments.value = segments.value.map { if (it.id == id) it.copy(feedback = feedback) else it }
    }

    override fun observeBetween(from: Instant, to: Instant): Flow<List<Segment>> = segments.map { all ->
        all.filter { !it.start.isBefore(from) && it.start.isBefore(to) }.sortedBy { it.start }
    }

    override suspend fun all(): List<Segment> = segments.value
}
