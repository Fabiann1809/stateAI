package com.stateai.ui.picker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.BuildConfig
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.title

@Composable
fun PickerRoute(onActivitySelected: (ActivityId) -> Unit, onNewActivity: () -> Unit, onOpenDebug: () -> Unit) {
    val container = appContainer()
    val viewModel: PickerViewModel = viewModel(
        factory = viewModelFactory { initializer { PickerViewModel(container.activityRepository) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PickerScreen(
        state = state,
        onActivitySelected = onActivitySelected,
        onNewActivity = onNewActivity,
        onOpenDebug = onOpenDebug.takeIf { BuildConfig.DEBUG },
    )
}

@Composable
fun PickerScreen(
    state: PickerUiState,
    onActivitySelected: (ActivityId) -> Unit,
    onNewActivity: () -> Unit,
    onOpenDebug: (() -> Unit)?,
) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.picker_title)) } }
            if (state.activities.isEmpty()) {
                item { Text(stringResource(R.string.picker_empty)) }
            }
            items(state.activities, key = { it.id.value }) { activity ->
                ActivityButton(activity = activity, onClick = { onActivitySelected(activity.id) })
            }
            item { NewActivityButton(enabled = state.canCreateNew, onClick = onNewActivity) }
            onOpenDebug?.let { open -> item { DebugButton(onClick = open) } }
        }
    }
}

@Composable
private fun ActivityButton(activity: Activity, onClick: () -> Unit) {
    val categoryLabel = stringResource(activity.category.labelRes())
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(activity.title()) },
        secondaryLabel = activity.name?.let { { Text(categoryLabel) } },
    )
}

@Composable
private fun NewActivityButton(enabled: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.picker_new)) },
        secondaryLabel = if (enabled) null else ({ Text(stringResource(R.string.picker_limit_reached)) }),
    )
}

@Composable
private fun DebugButton(onClick: () -> Unit) {
    CompactButton(onClick = onClick, label = { Text(stringResource(R.string.debug_open)) })
}
