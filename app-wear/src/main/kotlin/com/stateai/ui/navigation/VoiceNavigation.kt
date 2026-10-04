package com.stateai.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.composable
import com.stateai.ui.voice.VoiceExits
import com.stateai.ui.voice.VoiceRoute

/**
 * Voice entry, opened by tapping the mascot. It ends in a session, in "Nueva actividad" prefilled
 * with what was understood, or back on the home list (every failure).
 */
fun NavGraphBuilder.voiceDestination(navController: NavHostController) {
    composable(Routes.VOICE) {
        VoiceRoute(
            VoiceExits(
                onStart = { id -> navController.navigate(Routes.session(id)) { popUpTo(Routes.HOME) } },
                onReview = { category, name ->
                    navController.navigate(Routes.newActivity(category, name)) { popUpTo(Routes.HOME) }
                },
                onFallback = { navController.popBackStack(Routes.HOME, inclusive = false) },
            ),
        )
    }
}
