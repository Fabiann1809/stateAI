package com.stateai.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.composable
import com.stateai.domain.segment.SegmentId
import com.stateai.ui.feedback.FeedbackRoute
import com.stateai.ui.sessionsummary.SessionSummaryRoute

/** After a session ends: "how did you feel?" (answer or skip), then the session summary, then the picker. */
fun NavGraphBuilder.sessionEndDestinations(navController: NavHostController) {
    composable(Routes.FEEDBACK) { entry ->
        val segmentId = SegmentId(entry.idArgument())
        FeedbackRoute(segmentId = segmentId, onDone = {
            navController.navigate(Routes.sessionSummary(segmentId)) { popUpTo(Routes.PICKER) }
        })
    }
    composable(Routes.SESSION_SUMMARY) { entry ->
        SessionSummaryRoute(
            segmentId = SegmentId(entry.idArgument()),
            onDone = { navController.popBackStack(Routes.PICKER, inclusive = false) },
        )
    }
}
