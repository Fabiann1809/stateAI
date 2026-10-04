package com.stateai.domain.voice

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.activity.NameSimilarity

/** What to do with a spoken request once it is understood. */
sealed interface VoiceMatch {
    /** An activity that already exists (same or similar name): confirm and start it. */
    data class Existing(val activity: Activity) : VoiceMatch

    /** A clear request for an activity that does not exist yet: confirm, create and start it. */
    data class New(val category: ActivityCategory, val name: String?) : VoiceMatch

    /** Understood only in part: open "Nueva actividad" prefilled with what was understood. */
    data class Review(val category: ActivityCategory?, val name: String?) : VoiceMatch

    /** Nothing usable was said: fall back to the activity list. */
    data object NotUnderstood : VoiceMatch
}

/**
 * Matches a [VoiceIntent] with the person's activities. Names are compared with [NameSimilarity],
 * within the same category when the category is known. Without a match, a confident request becomes
 * a new activity and an unsure one is sent to review, never created silently.
 */
class ActivityMatcher(private val minConfidence: Double = DEFAULT_MIN_CONFIDENCE) {
    fun match(intent: VoiceIntent, activities: List<Activity>): VoiceMatch {
        if (intent.isEmpty || intent.confidence <= 0.0) return VoiceMatch.NotUnderstood
        val existing = findExisting(intent, activities)
        val category = intent.category
        return when {
            existing != null -> VoiceMatch.Existing(existing)
            category != null && intent.confidence >= minConfidence -> VoiceMatch.New(category, intent.name)
            else -> VoiceMatch.Review(category, intent.name)
        }
    }

    private fun findExisting(intent: VoiceIntent, activities: List<Activity>): Activity? {
        val candidates = activities.filter { intent.category == null || it.category == intent.category }
        val name = intent.name?.let(ActivityName::of)
        return if (name != null) bestNamed(name, candidates) else unnamed(intent, candidates)
    }

    private fun bestNamed(name: ActivityName, candidates: List<Activity>): Activity? = candidates
        .mapNotNull { activity -> activity.name?.let { activity to NameSimilarity.score(name, it) } }
        .filter { (_, score) -> score >= NameSimilarity.SIMILAR }
        .maxByOrNull { (_, score) -> score }
        ?.first

    /** "voy a leer" with no name means the category's unnamed activity, if there is one. */
    private fun unnamed(intent: VoiceIntent, candidates: List<Activity>): Activity? =
        if (intent.category != null) candidates.firstOrNull { it.name == null } else null

    private companion object {
        const val DEFAULT_MIN_CONFIDENCE = 0.6
    }
}
