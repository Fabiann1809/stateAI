package com.stateai.ui.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stateai.common.clockTicks
import com.stateai.di.appContainer
import kotlin.time.Duration.Companion.seconds

/** The [HomeViewModel] of the current screen (home and the activity list each get their own). */
@Composable
fun homeViewModel(): HomeViewModel {
    val container = appContainer()
    return viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    container.activityRepository,
                    container.insights.observeSuggestedActivity(),
                    container.insights.focusWindowNotifier.observe(),
                    container.insights.observeEnergy(clockTicks(container.clock, ENERGY_REFRESH)),
                    container.mascotRepository,
                )
            }
        },
    )
}

private val ENERGY_REFRESH = 30.seconds
