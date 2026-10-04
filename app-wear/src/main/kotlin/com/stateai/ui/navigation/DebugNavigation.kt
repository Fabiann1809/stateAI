package com.stateai.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.composable
import com.stateai.ui.debug.DebugMenuScreen
import com.stateai.ui.debug.HapticsDebugScreen
import com.stateai.ui.debug.MascotDebugScreen
import com.stateai.ui.debug.SensorsDebugScreen

/** Debug tools; only reachable from the home screen in debug builds. */
fun NavGraphBuilder.debugDestinations(navController: NavHostController) {
    composable(Routes.DEBUG_MENU) {
        DebugMenuScreen(
            onOpenHaptics = { navController.navigate(Routes.DEBUG_HAPTICS) },
            onOpenSensors = { navController.navigate(Routes.DEBUG_SENSORS) },
            onOpenMascot = { navController.navigate(Routes.DEBUG_MASCOT) },
        )
    }
    composable(Routes.DEBUG_HAPTICS) { HapticsDebugScreen() }
    composable(Routes.DEBUG_SENSORS) { SensorsDebugScreen() }
    composable(Routes.DEBUG_MASCOT) { MascotDebugScreen() }
}
