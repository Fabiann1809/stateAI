package com.stateai.ui.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.composable
import com.stateai.domain.activity.ActivityCategory
import com.stateai.ui.home.ActivitiesRoute
import com.stateai.ui.home.HomeRoute
import com.stateai.ui.newactivity.NewActivityPrefill
import com.stateai.ui.newactivity.NewActivityRoute

/** Ways into a session: the home screen (mascot), the activity list and "Nueva actividad". */
fun NavGraphBuilder.entryDestinations(navController: NavHostController) {
    composable(Routes.HOME) {
        HomeRoute(
            onTalk = { navController.navigate(Routes.VOICE) },
            onOpenActivities = { navController.navigate(Routes.ACTIVITIES) },
            onOpenSummary = { navController.navigate(Routes.SUMMARY) },
            onOpenDebug = { navController.navigate(Routes.DEBUG_MENU) },
        )
    }
    composable(Routes.ACTIVITIES) {
        ActivitiesRoute(
            onActivitySelected = { navController.navigate(Routes.session(it)) { popUpTo(Routes.HOME) } },
            onNewActivity = { navController.navigate(Routes.newActivity()) { popUpTo(Routes.HOME) } },
        )
    }
    composable(Routes.NEW_ACTIVITY, arguments = optionalText(Routes.ARG_CATEGORY, Routes.ARG_NAME)) { entry ->
        NewActivityRoute(prefill = entry.newActivityPrefill(), onActivityReady = { id ->
            navController.navigate(Routes.session(id)) { popUpTo(Routes.HOME) }
        })
    }
}

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
