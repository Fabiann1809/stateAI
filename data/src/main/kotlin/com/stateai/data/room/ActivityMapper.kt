package com.stateai.data.room

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.activity.ActivityStatus

internal fun Activity.toEntity(createdAtMillis: Long): ActivityEntity = ActivityEntity(
    id = id.value,
    category = category.name,
    displayName = name?.display,
    normalizedName = name?.normalized,
    status = status.name,
    createdAtMillis = createdAtMillis,
    lastUsedAtMillis = null,
)

internal fun ActivityEntity.toDomain(): Activity = Activity(
    id = ActivityId(id),
    category = ActivityCategory.valueOf(category),
    name = displayName?.let(ActivityName::of),
    status = ActivityStatus.valueOf(status),
)
