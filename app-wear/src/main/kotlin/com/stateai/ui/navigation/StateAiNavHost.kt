package com.stateai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.home.HomeRoute
import com.stateai.ui.newactivity.NewActivityPrefill
import com.stateai.ui.newactivity.NewActivityRoute
import com.stateai.ui.pause.PauseRoute
import com.stateai.ui.session.SessionRoute
import com.stateai.ui.summary.SummaryRoute

@Composable
fun StateAiNavHost() {
    val navController = rememberSwipeDismissableNavController()
    val backToHome = { navController.popBackStack(Routes.HOME, inclusive = false) }

    SwipeDismissableNavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeRoute(
                onTalk = { navController.navigate(Routes.VOICE) },
                onActivitySelected = { navController.navigate(Routes.session(it)) },
                onNewActivity = { navController.navigate(Routes.newActivity()) },
                onOpenSummary = { navController.navigate(Routes.SUMMARY) },
                onOpenDebug = { navController.navigate(Routes.DEBUG_MENU) },
            )
        }
        composable(Routes.NEW_ACTIVITY, arguments = optionalText(Routes.ARG_CATEGORY, Routes.ARG_NAME)) { entry ->
            NewActivityRoute(prefill = entry.newActivityPrefill(), onActivityReady = { id ->
                navController.navigate(Routes.session(id)) { popUpTo(Routes.HOME) }
            })
        }
        composable(Routes.SESSION) { entry ->
            SessionRoute(
                activityId = ActivityId(entry.idArgument()),
                onPause = { navController.navigate(Routes.PAUSE) },
                onStopped = { segmentId ->
                    if (segmentId == null) {
                        backToHome()
                    } else {
                        navController.navigate(Routes.feedback(segmentId)) { popUpTo(Routes.HOME) }
                    }
                },
            )
        }
        sessionEndDestinations(navController)
        composable(Routes.SUMMARY) { SummaryRoute() }
        voiceDestination(navController)
        composable(Routes.PAUSE) { PauseRoute(onFinished = { navController.popBackStack() }) }
        debugDestinations(navController)
    }
}

internal fun NavBackStackEntry.idArgument(): String = arguments?.getString(Routes.ARG_ID).orEmpty()

private fun NavBackStackEntry.newActivityPrefill(): NewActivityPrefill {
    val category = arguments?.getString(Routes.ARG_CATEGORY).orEmpty()
    return NewActivityPrefill(
        category = ActivityCategory.entries.firstOrNull { it.name == category },
        name = arguments?.getString(Routes.ARG_NAME)?.takeIf { it.isNotBlank() },
    )
}

private fun optionalText(vararg names: String) = names.map { name ->
    navArgument(name) {
        type = NavType.StringType
        defaultValue = ""
    }
}
