package com.stateai.ui.newactivity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.di.appContainer
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.common.icon
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.rememberTextInput
import com.stateai.ui.components.GradientButton
import com.stateai.ui.components.RoundIconButton
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.home.ListCard
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
            Icon(
                category.icon(),
                contentDescription = null,
                tint = StateAiColors.Accent2,
                modifier = Modifier.size(ICON),
            )
            Text(stringResource(category.labelRes()), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Box(Modifier.size(RADIO).border(1.5.dp, StateAiColors.Muted, CircleShape))
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
            GradientButton(
                label = stringResource(R.string.new_activity_use_name, name),
                onClick = { actions.onUseSuggested(name) },
                modifier = Modifier.fillMaxWidth(),
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
        }
    }
    item {
        RoundIconButton(
            StateAiIcons.Mic,
            stringResource(R.string.new_activity_add_name),
            actions.onAddName,
            primary = true,
        )
    }
    item {
        SurfaceButton(onClick = actions.onWithoutName) {
            Text(stringResource(R.string.new_activity_without_name), fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Full-width translucent card with free content in a row. */
@Composable
private fun SurfaceButton(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    ListCard(
        onClick = onClick,
        container = StateAiColors.Surface2,
        border = BorderStroke(1.dp, StateAiColors.Outline),
        content = content,
    )
}

private val ICON = 18.dp
private val RADIO = 11.dp
