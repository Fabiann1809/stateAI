package com.stateai.ui.newactivity

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.rememberTextInput

@Composable
fun NewActivityRoute(onActivityReady: (ActivityId) -> Unit) {
    val container = appContainer()
    val viewModel: NewActivityViewModel = viewModel(
        factory = viewModelFactory { initializer { NewActivityViewModel(container.createActivity) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.readyActivityId) { state.readyActivityId?.let(onActivityReady) }

    val askName = rememberTextInput(label = stringResource(R.string.new_activity_name_label)) { viewModel.create(it) }
    NewActivityScreen(
        state = state,
        onCategorySelected = viewModel::selectCategory,
        onAddName = askName,
        onWithoutName = { viewModel.create(null) },
    )
}

@Composable
fun NewActivityScreen(
    state: NewActivityUiState,
    onCategorySelected: (ActivityCategory) -> Unit,
    onAddName: () -> Unit,
    onWithoutName: () -> Unit,
) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            val category = state.category
            when {
                state.limitReached -> item { Text(stringResource(R.string.picker_limit_reached)) }
                category == null -> categoryStep(onCategorySelected)
                else -> nameStep(category, onAddName, onWithoutName)
            }
        }
    }
}

private fun ScalingLazyListScope.categoryStep(onCategorySelected: (ActivityCategory) -> Unit) {
    item { ListHeader { Text(stringResource(R.string.new_activity_category_title)) } }
    items(ActivityCategory.entries) { category ->
        Button(
            onClick = { onCategorySelected(category) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(category.labelRes())) },
        )
    }
}

private fun ScalingLazyListScope.nameStep(
    category: ActivityCategory,
    onAddName: () -> Unit,
    onWithoutName: () -> Unit,
) {
    item { ListHeader { Text(stringResource(category.labelRes())) } }
    item {
        Button(
            onClick = onAddName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.new_activity_add_name)) },
        )
    }
    item {
        FilledTonalButton(
            onClick = onWithoutName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.new_activity_without_name)) },
        )
    }
}
