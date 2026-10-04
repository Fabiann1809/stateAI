package com.stateai.ui.common

import kotlin.time.Duration

/** Formats as "m:ss" below one hour and "h:mm:ss" from one hour on. */
fun Duration.toClockText(): String = toComponents { hours, minutes, seconds, _ ->
    if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

/** Formats as "40 min" below one hour and "1 h 40 min" (or "2 h") from one hour on. */
fun Duration.toHoursMinutesText(): String = toComponents { hours, minutes, _, _ ->
    when {
        hours == 0L -> "$minutes min"
        minutes == 0 -> "$hours h"
        else -> "$hours h $minutes min"
    }
}
