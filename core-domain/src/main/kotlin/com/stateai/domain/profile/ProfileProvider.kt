package com.stateai.domain.profile

import com.stateai.domain.activity.Activity
import kotlin.time.Duration

/** Resolves the parameters that apply to an activity right now. */
fun interface ProfileProvider {
    fun targetBlockFor(activity: Activity): Duration
}

/** Uses the category defaults until learned activity profiles exist. */
class CategoryDefaultsProfileProvider : ProfileProvider {
    override fun targetBlockFor(activity: Activity): Duration =
        DefaultCategoryProfiles.of(activity.category).targetBlock
}
