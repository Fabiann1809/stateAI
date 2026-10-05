package com.stateai.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.stateai.domain.segment.SegmentId
import com.stateai.ui.feedback.FeedbackRoute
import com.stateai.ui.sessionsummary.SessionSummaryRoute

/** After a session ends: "how did you feel?" (answer or skip), then the session summary, then the home screen. */
fun NavGraphBuilder.sessionEndDestinations(navController: NavHostController) {
    screen(Routes.FEEDBACK) { entry ->
        val segmentId = SegmentId(entry.idArgument())
        FeedbackRoute(segmentId = segmentId, onDone = {
            navController.navigate(Routes.sessionSummary(segmentId)) { popUpTo(Routes.HOME) }
        })
    }
    screen(Routes.SESSION_SUMMARY) { entry ->
        SessionSummaryRoute(
            segmentId = SegmentId(entry.idArgument()),
            onDone = { navController.popBackStack(Routes.HOME, inclusive = false) },
        )
    }
}
