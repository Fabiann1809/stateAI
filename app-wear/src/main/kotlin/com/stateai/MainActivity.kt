package com.stateai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.stateai.ui.StateAiApp
import com.stateai.ui.ambient.AmbientObserver
import com.stateai.ui.ambient.LocalIsAmbient

class MainActivity : ComponentActivity() {
    private val ambientObserver = AmbientObserver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalIsAmbient provides ambientObserver.isAmbient) {
                StateAiApp()
            }
        }
    }
}
