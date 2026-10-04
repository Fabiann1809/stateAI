package com.stateai.domain.haptics

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** Global vibration limits (SPEC 6.2). The hourly cap comes from the activity profile. */
data class HapticLimits(val minInterval: Duration = 5.minutes, val capWindow: Duration = 1.hours)
