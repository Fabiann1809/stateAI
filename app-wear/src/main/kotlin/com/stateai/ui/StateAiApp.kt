package com.stateai.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import com.stateai.ui.navigation.StateAiNavHost
import com.stateai.ui.theme.StateAiTheme

/** Root composable of the watch app. */
@Composable
fun StateAiApp() {
    StateAiTheme {
        AppScaffold {
            StateAiNavHost()
        }
    }
}
