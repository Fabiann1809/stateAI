package com.stateai.domain.testing

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Builds a segment for tests; every field has a neutral default. */
@Suppress("LongParameterList")
fun testSegment(
    id: String = "segment",
    activity: Activity = Activity(ActivityId("activity"), ActivityCategory.STUDY, name = null),
    start: Instant = Instant.parse("2026-10-04T09:00:00Z"),
    duration: Duration = 40.minutes,
    planned: Duration = 40.minutes,
    levelTime: LevelDurations = LevelDurations(low = duration),
    trace: LevelTrace = LevelTrace("L".repeat(duration.inWholeMinutes.toInt())),
    calmHeartRate: Double? = null,
    cleanMovement: Double? = null,
) = Segment(
    id = SegmentId(id),
    activity = activity,
    start = start,
    end = start.plusSeconds(duration.inWholeSeconds),
    planned = planned,
    levelTime = levelTime,
    restlessTime = Duration.ZERO,
    pauseSuggestions = 0,
    pauses = emptyList(),
    trace = trace,
    calmHeartRate = calmHeartRate,
    cleanMovement = cleanMovement,
)
