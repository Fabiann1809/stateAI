package com.stateai.data.room

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.segment.Feedback
import com.stateai.domain.segment.LevelDurations
import com.stateai.domain.segment.LevelTrace
import com.stateai.domain.segment.PauseRecord
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.state.ActivationLevel
import com.stateai.domain.state.StateEstimate
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal fun Segment.toEntity(): SegmentEntity = SegmentEntity(
    id = id.value,
    activityId = activity.id.value,
    category = activity.category.name,
    displayName = activity.name?.display,
    startMillis = start.toEpochMilli(),
    endMillis = end.toEpochMilli(),
    plannedSeconds = planned.inWholeSeconds,
    lowSeconds = levelTime.low.inWholeSeconds,
    mediumSeconds = levelTime.medium.inWholeSeconds,
    highSeconds = levelTime.high.inWholeSeconds,
    unknownSeconds = levelTime.unknown.inWholeSeconds,
    restlessSeconds = restlessTime.inWholeSeconds,
    pauseSuggestions = pauseSuggestions,
    feedback = feedback?.name,
    traceSymbols = trace.symbols,
    cueMinutes = cueMinutes.joinToString(CUE_SEPARATOR),
    calmHeartRate = calmHeartRate,
    cleanMovement = cleanMovement,
)

private const val CUE_SEPARATOR = ","

internal fun PauseRecord.toEntity(segmentId: String): PauseEntity = PauseEntity(
    segmentId = segmentId,
    startMillis = start.toEpochMilli(),
    endMillis = end.toEpochMilli(),
    beforeLevel = before?.level?.name,
    beforeRestless = before?.restless,
    afterLevel = after?.level?.name,
    afterRestless = after?.restless,
)

internal fun SegmentWithPauses.toDomain(): Segment = Segment(
    id = SegmentId(segment.id),
    activity = Activity(
        ActivityId(segment.activityId),
        ActivityCategory.valueOf(segment.category),
        segment.displayName?.let(ActivityName::of),
    ),
    start = Instant.ofEpochMilli(segment.startMillis),
    end = Instant.ofEpochMilli(segment.endMillis),
    planned = segment.plannedSeconds.seconds,
    levelTime = LevelDurations(
        low = segment.lowSeconds.seconds,
        medium = segment.mediumSeconds.seconds,
        high = segment.highSeconds.seconds,
        unknown = segment.unknownSeconds.seconds,
    ),
    restlessTime = segment.restlessSeconds.seconds,
    pauseSuggestions = segment.pauseSuggestions,
    pauses = pauses.sortedBy { it.startMillis }.map { it.toDomain() },
    feedback = segment.feedback?.let(Feedback::valueOf),
    trace = LevelTrace(segment.traceSymbols),
    cueMinutes = segment.cueMinutes.split(CUE_SEPARATOR).filter { it.isNotBlank() }.map { it.toInt() },
    calmHeartRate = segment.calmHeartRate,
    cleanMovement = segment.cleanMovement,
)

private fun PauseEntity.toDomain(): PauseRecord = PauseRecord(
    start = Instant.ofEpochMilli(startMillis),
    end = Instant.ofEpochMilli(endMillis),
    before = estimateOf(beforeLevel, beforeRestless),
    after = estimateOf(afterLevel, afterRestless),
)

private fun estimateOf(level: String?, restless: Boolean?): StateEstimate? =
    level?.let { StateEstimate(ActivationLevel.valueOf(it), restless ?: false) }
