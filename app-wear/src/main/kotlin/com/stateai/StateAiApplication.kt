package com.stateai

import android.app.Application
import com.stateai.di.AppContainer

class StateAiApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
