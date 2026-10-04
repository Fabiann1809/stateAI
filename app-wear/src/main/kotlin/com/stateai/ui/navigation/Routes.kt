package com.stateai.ui.navigation

import android.net.Uri
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.SegmentId

/** Navigation destinations of the watch app. */
object Routes {
    const val HOME = "home"
    const val DEBUG_MENU = "debug"
    const val DEBUG_HAPTICS = "debug/haptics"
    const val DEBUG_SENSORS = "debug/sensors"
    const val DEBUG_MASCOT = "debug/mascot"
    const val ARG_CATEGORY = "category"
    const val ARG_NAME = "name"
    const val NEW_ACTIVITY = "activity/new?$ARG_CATEGORY={$ARG_CATEGORY}&$ARG_NAME={$ARG_NAME}"
    const val SUMMARY = "summary"
    const val PAUSE = "pause"
    const val VOICE = "voice"
    const val ARG_ID = "id"
    const val SESSION = "session/{$ARG_ID}"
    const val FEEDBACK = "feedback/{$ARG_ID}"
    const val SESSION_SUMMARY = "summary/session/{$ARG_ID}"

    /** "Nueva actividad", optionally prefilled with what a voice request understood. */
    fun newActivity(category: ActivityCategory? = null, name: String? = null): String =
        "activity/new?$ARG_CATEGORY=${category?.name.orEmpty()}&$ARG_NAME=${Uri.encode(name.orEmpty())}"

    fun session(activityId: ActivityId): String = "session/${activityId.value}"

    fun feedback(segmentId: SegmentId): String = "feedback/${segmentId.value}"

    fun sessionSummary(segmentId: SegmentId): String = "summary/session/${segmentId.value}"
}
