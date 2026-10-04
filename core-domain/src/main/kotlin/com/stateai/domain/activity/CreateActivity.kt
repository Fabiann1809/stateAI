package com.stateai.domain.activity

/** Outcome of creating an activity. */
sealed interface CreateActivityResult {
    data class Created(val activity: Activity) : CreateActivityResult

    /** An activity with the same category and normalized name already exists; it is reused. */
    data class Existing(val activity: Activity) : CreateActivityResult

    data object LimitReached : CreateActivityResult
}

/** Creates an activity from a category and an optional free name, avoiding duplicates. */
class CreateActivity(private val repository: ActivityRepository, private val newId: () -> ActivityId) {
    suspend operator fun invoke(category: ActivityCategory, rawName: String?): CreateActivityResult {
        val name = rawName?.let(ActivityName::of)
        val existing = repository.findByKey(ActivityKey(category, name))
        return when {
            existing != null -> CreateActivityResult.Existing(existing)
            repository.countActive() >= ActivityLimits.MAX_ACTIVE -> CreateActivityResult.LimitReached
            else -> CreateActivityResult.Created(add(category, name))
        }
    }

    private suspend fun add(category: ActivityCategory, name: ActivityName?): Activity {
        val activity = Activity(newId(), category, name)
        repository.add(activity)
        return activity
    }
}
