package com.stateai.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val category: String,
    val displayName: String?,
    val normalizedName: String?,
    val status: String,
    val createdAtMillis: Long,
    val lastUsedAtMillis: Long?,
)
