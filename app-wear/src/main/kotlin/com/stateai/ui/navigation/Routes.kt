package com.stateai.ui.navigation

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.SegmentId

/** Navigation destinations of the watch app. */
object Routes {
    const val PICKER = "picker"
    const val DEBUG_MENU = "debug"
    const val DEBUG_HAPTICS = "debug/haptics"
    const val DEBUG_SENSORS = "debug/sensors"
    const val NEW_ACTIVITY = "activity/new"
    const val SUMMARY = "summary"
    const val PAUSE = "pause"
    const val ARG_ID = "id"
    const val SESSION = "session/{$ARG_ID}"
    const val FEEDBACK = "feedback/{$ARG_ID}"
    const val SESSION_SUMMARY = "summary/session/{$ARG_ID}"

    fun session(activityId: ActivityId): String = "session/${activityId.value}"

    fun feedback(segmentId: SegmentId): String = "feedback/${segmentId.value}"

    fun sessionSummary(segmentId: SegmentId): String = "summary/session/${segmentId.value}"
}
