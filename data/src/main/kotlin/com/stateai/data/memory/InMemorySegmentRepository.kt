package com.stateai.data.memory

import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Volatile segment storage; used in tests and previews. */
class InMemorySegmentRepository : SegmentRepository {
    private val segments = MutableStateFlow<List<Segment>>(emptyList())

    override suspend fun save(segment: Segment) {
        segments.update { all -> all.filterNot { it.id == segment.id } + segment }
    }

    override suspend fun setFeedback(id: SegmentId, feedback: Feedback) {
        segments.update { all -> all.map { if (it.id == id) it.copy(feedback = feedback) else it } }
    }

    override suspend fun find(id: SegmentId): Segment? = segments.value.firstOrNull { it.id == id }

    override fun observeBetween(from: Instant, to: Instant): Flow<List<Segment>> = segments.map { all ->
        all.filter { !it.start.isBefore(from) && it.start.isBefore(to) }.sortedBy { it.start }
    }

    override suspend fun all(): List<Segment> = segments.value
}
