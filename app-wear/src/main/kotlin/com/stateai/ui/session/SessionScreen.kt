package com.stateai.ui.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.di.appContainer
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.common.title

@Composable
fun SessionRoute(activityId: ActivityId) {
    val repository = appContainer().activityRepository
    var activity by remember { mutableStateOf<Activity?>(null) }
    LaunchedEffect(activityId) { activity = repository.findById(activityId) }

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            activity?.let { Text(it.title()) }
        }
    }
}
