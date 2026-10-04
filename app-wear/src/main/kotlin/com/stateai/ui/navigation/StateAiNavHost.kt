package com.stateai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.segment.SegmentId
import com.stateai.ui.feedback.FeedbackRoute
import com.stateai.ui.newactivity.NewActivityRoute
import com.stateai.ui.pause.PauseRoute
import com.stateai.ui.picker.PickerRoute
import com.stateai.ui.session.SessionRoute
import com.stateai.ui.summary.SummaryRoute

@Composable
fun StateAiNavHost() {
    val navController = rememberSwipeDismissableNavController()
    val backToPicker = { navController.popBackStack(Routes.PICKER, inclusive = false) }

    SwipeDismissableNavHost(navController = navController, startDestination = Routes.PICKER) {
        composable(Routes.PICKER) {
            PickerRoute(
                onActivitySelected = { navController.navigate(Routes.session(it)) },
                onNewActivity = { navController.navigate(Routes.NEW_ACTIVITY) },
                onOpenSummary = { navController.navigate(Routes.SUMMARY) },
                onOpenDebug = { navController.navigate(Routes.DEBUG_MENU) },
            )
        }
        composable(Routes.NEW_ACTIVITY) {
            NewActivityRoute(onActivityReady = { id ->
                navController.navigate(Routes.session(id)) { popUpTo(Routes.PICKER) }
            })
        }
        composable(Routes.SESSION) { entry ->
            SessionRoute(
                activityId = ActivityId(entry.idArgument()),
                onPause = { navController.navigate(Routes.PAUSE) },
                onStopped = { segmentId ->
                    if (segmentId == null) {
                        backToPicker()
                    } else {
                        navController.navigate(Routes.feedback(segmentId)) { popUpTo(Routes.PICKER) }
                    }
                },
            )
        }
        composable(Routes.FEEDBACK) { entry ->
            FeedbackRoute(segmentId = SegmentId(entry.idArgument()), onDone = { backToPicker() })
        }
        composable(Routes.SUMMARY) { SummaryRoute() }
        composable(Routes.PAUSE) { PauseRoute(onFinished = { navController.popBackStack() }) }
        debugDestinations(navController)
    }
}

private fun NavBackStackEntry.idArgument(): String = arguments?.getString(Routes.ARG_ID).orEmpty()
