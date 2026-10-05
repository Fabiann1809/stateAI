package com.stateai.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** The silent way in: the suggested activity first, then the rest, and "Nueva". One tap starts a session. */
@Composable
fun ActivitiesRoute(onActivitySelected: (ActivityId) -> Unit, onNewActivity: () -> Unit) {
    val state by homeViewModel().uiState.collectAsStateWithLifecycle()
    ActivitiesScreen(state, onActivitySelected, onNewActivity)
}

@Composable
fun ActivitiesScreen(state: HomeUiState, onActivitySelected: (ActivityId) -> Unit, onNewActivity: () -> Unit) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader {
                    Text(
                        stringResource(R.string.activities_title),
                        fontSize = StateAiDimens.Body,
                        color = StateAiColors.Text3,
                    )
                }
            }
            if (state.isFocusWindow) item { HintText(stringResource(R.string.picker_focus_window)) }
            if (state.activities.isEmpty()) item { HintText(stringResource(R.string.picker_empty)) }
            items(state.activities, key = { it.id.value }) { activity ->
                ActivityCard(activity, suggested = activity == state.suggested, onClick = {
                    onActivitySelected(activity.id)
                })
            }
            item { NewActivityButton(enabled = state.canCreateNew, onClick = onNewActivity) }
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(text, fontSize = StateAiDimens.Label, color = StateAiColors.Accent, textAlign = TextAlign.Center)
}
