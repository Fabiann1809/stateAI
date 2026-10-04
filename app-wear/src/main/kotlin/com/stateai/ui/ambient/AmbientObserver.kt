package com.stateai.ui.ambient

import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.wear.ambient.AmbientLifecycleObserver

/** Tracks ambient mode for an activity and exposes it as Compose state. */
class AmbientObserver(activity: ComponentActivity) {
    private val ambient: MutableState<Boolean> = mutableStateOf(false)
    val isAmbient: Boolean get() = ambient.value

    private val callback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            ambient.value = true
        }

        override fun onExitAmbient() {
            ambient.value = false
        }
    }

    init {
        activity.lifecycle.addObserver(AmbientLifecycleObserver(activity, callback))
    }
}
