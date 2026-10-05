package com.stateai.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.wear.compose.navigation.composable
import com.stateai.ui.ambient.LocalIsAmbient
import com.stateai.ui.components.glowBackground

/**
 * A destination drawn on the app background. Each screen paints it itself because the swipe-to-dismiss
 * container needs every screen to be opaque, so a single background behind the nav host would stay hidden.
 */
internal fun NavGraphBuilder.screen(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(route, arguments) { entry ->
        Box(Modifier.fillMaxSize().glowBackground(LocalIsAmbient.current)) { content(entry) }
    }
}
