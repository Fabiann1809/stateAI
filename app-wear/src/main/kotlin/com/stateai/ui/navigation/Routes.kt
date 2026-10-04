package com.stateai.ui.navigation

import com.stateai.domain.activity.ActivityId

/** Navigation destinations of the watch app. */
object Routes {
    const val PICKER = "picker"
    const val DEBUG_MENU = "debug"
    const val DEBUG_HAPTICS = "debug/haptics"
    const val DEBUG_SENSORS = "debug/sensors"
    const val NEW_ACTIVITY = "activity/new"
    const val SESSION_ARG_ACTIVITY_ID = "activityId"
    const val SESSION = "session/{$SESSION_ARG_ACTIVITY_ID}"

    fun session(activityId: ActivityId): String = "session/${activityId.value}"
}
