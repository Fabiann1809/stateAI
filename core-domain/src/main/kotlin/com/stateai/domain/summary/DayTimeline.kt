package com.stateai.domain.summary

import com.stateai.domain.segment.Segment
import com.stateai.domain.state.DisplayState
import java.time.Instant

/** One point of the day chart: the predominant state of a block of minutes, at the block's middle. */
data class TimelinePoint(val time: Instant, val state: DisplayState?)

/**
 * The day as a few points instead of one per minute, so it fits on a watch: each segment's trace is
 * cut into blocks of [blockMinutes] windows and every block keeps its most frequent state (ties go
 * to the more activated state, so overload is not hidden). Blocks without any estimate are null.
 */
class DayTimeline(private val blockMinutes: Int = DEFAULT_BLOCK_MINUTES) {
    fun points(segments: List<Segment>): List<TimelinePoint> = segments.sortedBy { it.start }.flatMap { segment ->
        SessionReport.of(segment).minutes.chunked(blockMinutes).mapIndexed { index, block ->
            val middle = index * blockMinutes + block.size / 2.0
            TimelinePoint(segment.start.plusSeconds((middle * SECONDS_PER_MINUTE).toLong()), predominant(block))
        }
    }

    private fun predominant(block: List<DisplayState?>): DisplayState? = block.filterNotNull()
        .groupingBy { it }
        .eachCount()
        .maxWithOrNull(compareBy<Map.Entry<DisplayState, Int>> { it.value }.thenBy { it.key.ordinal })
        ?.key

    private companion object {
        const val DEFAULT_BLOCK_MINUTES = 10
        const val SECONDS_PER_MINUTE = 60
    }
}
