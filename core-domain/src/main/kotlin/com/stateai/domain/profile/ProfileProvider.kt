package com.stateai.domain.profile

import com.stateai.domain.activity.Activity

/** Resolves the profile parameters that apply to an activity right now. */
fun interface ProfileProvider {
    fun profileFor(activity: Activity): ActivityProfile
}

/** Uses the category defaults until learned activity profiles exist. */
class CategoryDefaultsProfileProvider : ProfileProvider {
    override fun profileFor(activity: Activity): ActivityProfile = DefaultCategoryProfiles.of(activity.category)
}
