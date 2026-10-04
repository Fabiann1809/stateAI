package com.stateai.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.BuildConfig
import com.stateai.R
import com.stateai.common.clockTicks
import com.stateai.di.appContainer
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.energy.EnergyBudget
import com.stateai.ui.common.labelRes
import com.stateai.ui.common.title
import com.stateai.ui.components.EnergySilhouette
import com.stateai.ui.components.StateAiIcons
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens
import com.stateai.ui.theme.tabular
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeRoute(
    onTalk: () -> Unit,
    onActivitySelected: (ActivityId) -> Unit,
    onNewActivity: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val container = appContainer()
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    container.activityRepository,
                    container.insights.observeSuggestedActivity(),
                    container.insights.focusWindowNotifier.observe(),
                    container.insights.observeEnergy(clockTicks(container.clock, ENERGY_REFRESH)),
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        actions = HomeActions(
            onTalk = onTalk,
            onActivitySelected = onActivitySelected,
            onNewActivity = onNewActivity,
            onOpenSummary = onOpenSummary,
            onOpenDebug = onOpenDebug.takeIf { BuildConfig.DEBUG },
        ),
    )
}

/** What the home screen lets the person do. [onOpenDebug] is null outside debug builds. */
data class HomeActions(
    val onTalk: () -> Unit,
    val onActivitySelected: (ActivityId) -> Unit,
    val onNewActivity: () -> Unit,
    val onOpenSummary: () -> Unit,
    val onOpenDebug: (() -> Unit)?,
)

@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions) {
    // Start centered on the mascot: it is the main way in; the list is just below.
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { MascotHeader(onTalk = actions.onTalk, onLongPress = actions.onOpenDebug) }
            if (state.isFocusWindow) item { HintText(stringResource(R.string.picker_focus_window)) }
            if (state.activities.isEmpty()) item { HintText(stringResource(R.string.picker_empty)) }
            items(state.activities, key = { it.id.value }) { activity ->
                ActivityCard(
                    activity = activity,
                    suggested = activity == state.suggested,
                    onClick = { actions.onActivitySelected(activity.id) },
                )
            }
            item { NewActivityButton(enabled = state.canCreateNew, onClick = actions.onNewActivity) }
            item { SummaryButton(onClick = actions.onOpenSummary) }
            state.energy?.let { energy -> item { MiniEnergy(energy) } }
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(text, fontSize = StateAiDimens.Label, color = StateAiColors.Accent, textAlign = TextAlign.Center)
}

@Composable
private fun ActivityCard(activity: Activity, suggested: Boolean, onClick: () -> Unit) {
    val categoryLabel = stringResource(activity.category.labelRes())
    val container = if (suggested) StateAiColors.Accent.copy(alpha = SUGGESTED_FILL) else StateAiColors.Surface2
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = StateAiColors.Text1,
        ),
        border = if (suggested) BorderStroke(1.dp, StateAiColors.Accent.copy(alpha = SUGGESTED_BORDER)) else null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            if (suggested) {
                Text(
                    text = stringResource(R.string.picker_suggested_tag),
                    fontSize = StateAiDimens.Label,
                    color = StateAiColors.Accent,
                )
            }
            Text(
                text = activity.title(),
                fontSize = if (suggested) TITLE_SIZE_SUGGESTED else TITLE_SIZE,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            if (!suggested && activity.name != null) {
                Text(categoryLabel, fontSize = StateAiDimens.Label, color = StateAiColors.Text2, maxLines = 1)
            }
        }
    }
}

@Composable
private fun NewActivityButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        icon = { Icon(StateAiIcons.Plus, contentDescription = null) },
        label = { Text(stringResource(R.string.picker_new), fontWeight = FontWeight.Medium) },
        secondaryLabel = if (enabled) null else ({ Text(stringResource(R.string.picker_limit_reached)) }),
    )
}

@Composable
private fun SummaryButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = StateAiColors.Surface2,
            contentColor = StateAiColors.Text1,
            iconColor = StateAiColors.Text1,
        ),
        icon = { Icon(StateAiIcons.Bars, contentDescription = null) },
        label = { Text(stringResource(R.string.summary_open)) },
    )
}

@Composable
private fun MiniEnergy(energy: EnergyBudget) {
    val percent = energy.level.roundToInt()
    Row(
        modifier = Modifier.padding(top = StateAiDimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(StateAiDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EnergySilhouette(
            level = energy.level,
            color = StateAiColors.Accent,
            height = MINI_ENERGY_HEIGHT,
            low = energy.isLow,
            description = stringResource(R.string.energy_description, percent),
        )
        Column {
            Text(
                text = stringResource(R.string.percent, percent),
                fontSize = MINI_ENERGY_TEXT,
                fontWeight = FontWeight.Medium,
                style = LocalTextStyle.current.tabular(),
            )
            Text(stringResource(R.string.energy_estimated), fontSize = StateAiDimens.Label, color = StateAiColors.Text2)
        }
    }
}

private const val SUGGESTED_FILL = 0.14f
private const val SUGGESTED_BORDER = 0.55f
private val TITLE_SIZE = 14.sp
private val TITLE_SIZE_SUGGESTED = 17.sp
private val MINI_ENERGY_HEIGHT = 52.dp
private val MINI_ENERGY_TEXT = 24.sp
private val ENERGY_REFRESH = 30.seconds
