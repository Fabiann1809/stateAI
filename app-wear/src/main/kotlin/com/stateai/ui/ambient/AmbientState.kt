package com.stateai.ui.ambient

import androidx.compose.runtime.compositionLocalOf

/** Whether the watch is in low-power ambient mode, provided by the activity to all screens. */
val LocalIsAmbient = compositionLocalOf { false }
