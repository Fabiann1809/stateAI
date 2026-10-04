package com.stateai.domain.profile

import com.stateai.domain.activity.ActivityCategory
import kotlin.time.Duration.Companion.minutes

/** Initial category profiles (SPEC 6.3). Target blocks are the midpoint of each specified range. */
object DefaultCategoryProfiles {
    val DEEP_WORK = ActivityProfile(
        category = ActivityCategory.DEEP_WORK,
        targetBlock = 75.minutes,
        sensitivity = Sensitivity.LOW,
        normalMovement = MovementLevel.LOW,
        maxVibrationsPerHour = 2,
    )
    val STUDY = ActivityProfile(
        category = ActivityCategory.STUDY,
        targetBlock = 40.minutes,
        sensitivity = Sensitivity.MEDIUM,
        normalMovement = MovementLevel.LOW,
        maxVibrationsPerHour = 3,
    )
    val READING = ActivityProfile(
        category = ActivityCategory.READING,
        targetBlock = 40.minutes,
        sensitivity = Sensitivity.HIGH,
        normalMovement = MovementLevel.VERY_LOW,
        maxVibrationsPerHour = 2,
    )
    val COLLAB = ActivityProfile(
        category = ActivityCategory.COLLAB,
        targetBlock = 45.minutes,
        sensitivity = Sensitivity.MEDIUM,
        normalMovement = MovementLevel.MEDIUM_HIGH,
        maxVibrationsPerHour = 4,
    )
    val OTHER = ActivityProfile(
        category = ActivityCategory.OTHER,
        targetBlock = 45.minutes,
        sensitivity = Sensitivity.MEDIUM,
        normalMovement = MovementLevel.MEDIUM,
        maxVibrationsPerHour = 3,
    )

    fun of(category: ActivityCategory): ActivityProfile = when (category) {
        ActivityCategory.DEEP_WORK -> DEEP_WORK
        ActivityCategory.STUDY -> STUDY
        ActivityCategory.READING -> READING
        ActivityCategory.COLLAB -> COLLAB
        ActivityCategory.OTHER -> OTHER
    }
}
