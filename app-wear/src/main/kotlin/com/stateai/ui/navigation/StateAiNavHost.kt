package com.stateai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.debug.DebugMenuScreen
import com.stateai.ui.debug.HapticsDebugScreen
import com.stateai.ui.debug.SensorsDebugScreen
import com.stateai.ui.newactivity.NewActivityRoute
import com.stateai.ui.picker.PickerRoute
import com.stateai.ui.session.SessionRoute

@Composable
fun StateAiNavHost() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(navController = navController, startDestination = Routes.PICKER) {
        composable(Routes.PICKER) {
            PickerRoute(
                onActivitySelected = { navController.navigate(Routes.session(it)) },
                onNewActivity = { navController.navigate(Routes.NEW_ACTIVITY) },
                onOpenDebug = { navController.navigate(Routes.DEBUG_MENU) },
            )
        }
        composable(Routes.NEW_ACTIVITY) {
            NewActivityRoute(onActivityReady = { id ->
                navController.navigate(Routes.session(id)) { popUpTo(Routes.PICKER) }
            })
        }
        composable(Routes.DEBUG_MENU) {
            DebugMenuScreen(
                onOpenHaptics = { navController.navigate(Routes.DEBUG_HAPTICS) },
                onOpenSensors = { navController.navigate(Routes.DEBUG_SENSORS) },
            )
        }
        composable(Routes.DEBUG_HAPTICS) { HapticsDebugScreen() }
        composable(Routes.DEBUG_SENSORS) { SensorsDebugScreen() }
        composable(Routes.SESSION) { entry ->
            val activityId = entry.arguments?.getString(Routes.SESSION_ARG_ACTIVITY_ID).orEmpty()
            SessionRoute(activityId = ActivityId(activityId), onStopped = { navController.popBackStack() })
        }
    }
}
