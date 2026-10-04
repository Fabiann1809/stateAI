package com.stateai.domain.profile

import com.stateai.domain.activity.Activity
import com.stateai.domain.learning.LearningRepository

/** Resolves the profile parameters that apply to an activity right now. */
fun interface ProfileProvider {
    suspend fun profileFor(activity: Activity): ActivityProfile
}

/** Category defaults blended with what the activity has learned so far. */
class LearnedProfileProvider(
    private val learning: LearningRepository,
    private val blender: ProfileBlender = ProfileBlender(),
) : ProfileProvider {
    override suspend fun profileFor(activity: Activity): ActivityProfile =
        blender.blend(DefaultCategoryProfiles.of(activity.category), learning.load(activity.id))
}
