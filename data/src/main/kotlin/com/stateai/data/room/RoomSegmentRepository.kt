package com.stateai.data.room

import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.segment.SegmentRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSegmentRepository(private val dao: SegmentDao) : SegmentRepository {
    override suspend fun save(segment: Segment) {
        dao.save(segment.toEntity(), segment.pauses.map { it.toEntity(segment.id.value) })
    }

    override suspend fun setFeedback(id: SegmentId, feedback: Feedback) {
        dao.setFeedback(id.value, feedback.name)
    }

    override suspend fun find(id: SegmentId): Segment? = dao.find(id.value)?.toDomain()

    override fun observeBetween(from: Instant, to: Instant): Flow<List<Segment>> =
        dao.observeBetween(from.toEpochMilli(), to.toEpochMilli()).map { rows -> rows.map { it.toDomain() } }

    override suspend fun all(): List<Segment> = dao.all().map { it.toDomain() }
}
