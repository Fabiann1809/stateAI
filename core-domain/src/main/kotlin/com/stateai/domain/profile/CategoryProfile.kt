package com.stateai.domain.profile

import com.stateai.domain.activity.ActivityCategory
import kotlin.time.Duration

/** Default parameters of a base category, used until an activity learns its own. */
data class CategoryProfile(
    val category: ActivityCategory,
    val targetBlock: Duration,
    val sensitivity: Sensitivity,
    val normalMovement: MovementLevel,
    val maxVibrationsPerHour: Int,
)
