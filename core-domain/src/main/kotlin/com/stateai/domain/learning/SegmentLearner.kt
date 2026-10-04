package com.stateai.domain.learning

import com.stateai.domain.segment.Segment

/** Learns something from a segment once it is closed and stored. */
fun interface SegmentLearner {
    suspend fun learn(segment: Segment)
}
