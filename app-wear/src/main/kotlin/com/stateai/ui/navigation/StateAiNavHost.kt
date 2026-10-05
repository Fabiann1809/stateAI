package com.stateai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.stateai.domain.activity.ActivityId
import com.stateai.ui.pause.PauseRoute
import com.stateai.ui.pause.RestRoute
import com.stateai.ui.session.SessionRoute
import com.stateai.ui.summary.SummaryRoute

@Composable
fun StateAiNavHost() {
    val navController = rememberSwipeDismissableNavController()
    val backToHome = { navController.popBackStack(Routes.HOME, inclusive = false) }

    SwipeDismissableNavHost(navController = navController, startDestination = Routes.HOME) {
        entryDestinations(navController)
        composable(Routes.SESSION) { entry ->
            SessionRoute(
                activityId = ActivityId(entry.idArgument()),
                onPause = { navController.navigate(Routes.PAUSE) },
                onRest = { navController.navigate(Routes.REST) },
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
        composable(Routes.REST) { RestRoute(onFinished = { navController.popBackStack() }) }
        debugDestinations(navController)
    }
}

internal fun NavBackStackEntry.idArgument(): String = arguments?.getString(Routes.ARG_ID).orEmpty()
