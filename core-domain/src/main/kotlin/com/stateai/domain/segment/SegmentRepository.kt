package com.stateai.domain.segment

import java.time.Instant
import kotlinx.coroutines.flow.Flow

/** Storage of closed segments (summaries only). */
interface SegmentRepository {
    suspend fun save(segment: Segment)

    suspend fun setFeedback(id: SegmentId, feedback: Feedback)

    /** Segments that started in [from, to), oldest first. */
    fun observeBetween(from: Instant, to: Instant): Flow<List<Segment>>

    suspend fun all(): List<Segment>
}
