package com.stateai.domain.profile

import com.stateai.domain.activity.ActivityCategory
import kotlin.time.Duration

/** Parameters that drive a session: category defaults, possibly blended with what an activity learned. */
data class ActivityProfile(
    val category: ActivityCategory,
    val targetBlock: Duration,
    val sensitivity: Sensitivity,
    val normalMovement: MovementLevel,
    val maxVibrationsPerHour: Int,
)
