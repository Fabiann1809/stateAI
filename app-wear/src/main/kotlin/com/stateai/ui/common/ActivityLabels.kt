package com.stateai.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.stateai.R
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory

@StringRes
fun ActivityCategory.labelRes(): Int = when (this) {
    ActivityCategory.DEEP_WORK -> R.string.category_deep_work
    ActivityCategory.STUDY -> R.string.category_study
    ActivityCategory.READING -> R.string.category_reading
    ActivityCategory.COLLAB -> R.string.category_collab
    ActivityCategory.OTHER -> R.string.category_other
}

/** The activity's own name, or its category when it has none. */
@Composable
fun Activity.title(): String = name?.display ?: stringResource(category.labelRes())
