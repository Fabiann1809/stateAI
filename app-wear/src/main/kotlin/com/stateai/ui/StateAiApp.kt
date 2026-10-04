package com.stateai.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import com.stateai.ui.navigation.StateAiNavHost

/** Root composable of the watch app. */
@Composable
fun StateAiApp() {
    MaterialTheme {
        AppScaffold {
            StateAiNavHost()
        }
    }
}
