package com.stateai

import android.app.Application
import com.stateai.di.AppContainer
import com.stateai.service.SessionService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StateAiApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        startServiceWhenSessionBegins()
    }

    /** The session service owns the foreground notification for as long as a session runs. */
    private fun startServiceWhenSessionBegins() {
        container.sessionTracker.activeSession
            .filterNotNull()
            .onEach { SessionService.start(this) }
            .launchIn(MainScope())
    }
}
