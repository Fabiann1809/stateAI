package com.stateai.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.stateai.StateAiApplication

/** The [AppContainer] of the running application, for use inside composables. */
@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as StateAiApplication).container
