package com.stateai.ui.newactivity

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.rememberTextInput
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

@Composable
fun NewActivityRoute(prefill: NewActivityPrefill, onActivityReady: (ActivityId) -> Unit) {
    val container = appContainer()
    val viewModel: NewActivityViewModel = viewModel(
        factory = viewModelFactory { initializer { NewActivityViewModel(container.createActivity, prefill) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.readyActivityId) { state.readyActivityId?.let(onActivityReady) }

    val askName = rememberTextInput(label = stringResource(R.string.new_activity_name_label)) { viewModel.create(it) }
    NewActivityScreen(
        state = state,
        onCategorySelected = viewModel::selectCategory,
        onAddName = askName,
        onWithoutName = { viewModel.create(null) },
        onUseSuggested = viewModel::create,
    )
}

@Composable
fun NewActivityScreen(
    state: NewActivityUiState,
    onCategorySelected: (ActivityCategory) -> Unit,
    onAddName: () -> Unit,
    onWithoutName: () -> Unit,
    onUseSuggested: (String) -> Unit = {},
) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            val category = state.category
            when {
                state.limitReached -> item { Text(stringResource(R.string.picker_limit_reached)) }
                category == null -> categoryStep(onCategorySelected)
                else -> nameStep(category, state.suggestedName, NameActions(onAddName, onWithoutName, onUseSuggested))
            }
        }
    }
}

private fun ScalingLazyListScope.categoryStep(onCategorySelected: (ActivityCategory) -> Unit) {
    item {
        ListHeader {
            Text(
                text = stringResource(R.string.new_activity_category_title),
                fontSize = StateAiDimens.Label,
                color = StateAiColors.Text3,
            )
        }
    }
    items(ActivityCategory.entries) { category ->
        SurfaceButton(onClick = { onCategorySelected(category) }) {
            Text(stringResource(category.labelRes()), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Box(Modifier.size(RADIO).border(1.5.dp, StateAiColors.NoData, CircleShape))
        }
    }
}

/** The three ways to name an activity: the name understood by voice (if any), typing it, or none. */
private class NameActions(
    val onAddName: () -> Unit,
    val onWithoutName: () -> Unit,
    val onUseSuggested: (String) -> Unit,
)

private fun ScalingLazyListScope.nameStep(category: ActivityCategory, suggestedName: String?, actions: NameActions) {
    item {
        ListHeader {
            Text(stringResource(category.labelRes()), fontSize = StateAiDimens.Label, color = StateAiColors.Accent)
        }
    }
    suggestedName?.let { name ->
        item {
            Button(
                onClick = { actions.onUseSuggested(name) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.new_activity_use_name, name), fontWeight = FontWeight.Medium) },
            )
        }
    }
    item {
        SurfaceButton(onClick = actions.onAddName) {
            Text(
                text = stringResource(R.string.new_activity_add_name),
                color = StateAiColors.Text2,
                modifier = Modifier.weight(1f),
            )
            Box(Modifier.size(width = 1.5.dp, height = CURSOR_HEIGHT).border(1.dp, StateAiColors.Accent))
        }
    }
    item {
        Button(
            onClick = actions.onWithoutName,
            colors = surfaceColors(),
            label = { Text(stringResource(R.string.new_activity_without_name), fontWeight = FontWeight.Medium) },
        )
    }
}

/** Full-width pill in the elevated surface color with free content in a row. */
@Composable
private fun SurfaceButton(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = surfaceColors(),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun surfaceColors() =
    ButtonDefaults.buttonColors(containerColor = StateAiColors.Surface2, contentColor = StateAiColors.Text1)

private val RADIO = 11.dp
private val CURSOR_HEIGHT = 18.dp
