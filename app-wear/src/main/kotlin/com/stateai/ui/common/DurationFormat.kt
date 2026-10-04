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
