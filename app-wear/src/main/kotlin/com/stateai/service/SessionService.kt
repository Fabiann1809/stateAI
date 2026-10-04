package com.stateai.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.stateai.StateAiApplication
import com.stateai.domain.session.ActiveSession
import com.stateai.ui.common.activityTitle
import kotlinx.coroutines.launch

/** Keeps the process alive while a session runs, so it continues with the screen off. */
class SessionService : LifecycleService() {
    private val container by lazy { (application as StateAiApplication).container }
    private val notifications by lazy { SessionNotificationFactory(this, container.clock) }

    override fun onCreate() {
        super.onCreate()
        notifications.createChannel()
        lifecycleScope.launch {
            container.sessionTracker.activeSession.collect { session ->
                if (session == null) stopSelf() else showForeground(session)
            }
        }
    }

    private fun showForeground(session: ActiveSession) {
        val notification = notifications.create(session, activityTitle(this, session.activity))
        ServiceCompat.startForeground(
            this,
            SessionNotificationFactory.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
        )
    }

    companion object {
        fun start(context: Context) {
            context.startForegroundService(Intent(context, SessionService::class.java))
        }
    }
}
