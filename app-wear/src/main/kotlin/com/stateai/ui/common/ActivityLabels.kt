package com.stateai.ui.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.stateai.R
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.ui.components.StateAiIcons

@StringRes
fun ActivityCategory.labelRes(): Int = when (this) {
    ActivityCategory.DEEP_WORK -> R.string.category_deep_work
    ActivityCategory.STUDY -> R.string.category_study
    ActivityCategory.READING -> R.string.category_reading
    ActivityCategory.COLLAB -> R.string.category_collab
    ActivityCategory.OTHER -> R.string.category_other
}

/** The activity's own name, or its category when it has none. */
fun activityTitle(context: Context, activity: Activity): String =
    activity.name?.display ?: context.getString(activity.category.labelRes())

@Composable
fun Activity.title(): String = activityTitle(LocalContext.current, this)

/** The outline icon of each category (the same in the activity list and when creating one). */
fun ActivityCategory.icon(): ImageVector = when (this) {
    ActivityCategory.DEEP_WORK -> StateAiIcons.DeepWork
    ActivityCategory.STUDY -> StateAiIcons.Study
    ActivityCategory.READING -> StateAiIcons.Reading
    ActivityCategory.COLLAB -> StateAiIcons.Collab
    ActivityCategory.OTHER -> StateAiIcons.Other
}
