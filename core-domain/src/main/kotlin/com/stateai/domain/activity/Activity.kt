package com.stateai.domain.activity

/**
 * Something the user does in a session: a base category plus an optional free name.
 * Without a name, the activity is the category itself.
 */
data class Activity(
    val id: ActivityId,
    val category: ActivityCategory,
    val name: ActivityName?,
    val status: ActivityStatus = ActivityStatus.ACTIVE,
) {
    /** Identity used to detect duplicates: same category and same normalized name. */
    val key: ActivityKey get() = ActivityKey(category, name)
}

/** Category and normalized name; two activities with the same key are the same activity. */
data class ActivityKey(val category: ActivityCategory, val name: ActivityName?)
