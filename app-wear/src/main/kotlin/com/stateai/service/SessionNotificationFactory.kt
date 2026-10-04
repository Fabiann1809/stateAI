package com.stateai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.stateai.MainActivity
import com.stateai.R
import com.stateai.domain.session.ActiveSession
import java.time.Clock

/** Builds the session notification and attaches a Wear Ongoing Activity to it. */
class SessionNotificationFactory(private val context: Context, private val clock: Clock) {
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.session_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun create(session: ActiveSession, title: String): Notification {
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_session)
            .setContentTitle(title)
            .setContentIntent(openApp)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setOngoing(true)
            .setSilent(true)

        OngoingActivity.Builder(context, NOTIFICATION_ID, builder)
            .setStaticIcon(R.drawable.ic_session)
            .setTouchIntent(openApp)
            .setStatus(elapsedStatus(session))
            .build()
            .apply(context)

        return builder.build()
    }

    /** A stopwatch that counts from the session start, rendered by the system without app updates. */
    private fun elapsedStatus(session: ActiveSession): Status {
        val elapsedMillis = session.progressAt(clock.instant()).elapsed.inWholeMilliseconds
        val startInElapsedRealtime = SystemClock.elapsedRealtime() - elapsedMillis
        return Status.Builder()
            .addTemplate(STATUS_TEMPLATE)
            .addPart(STATUS_PART, Status.StopwatchPart(startInElapsedRealtime))
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "session"
        private const val STATUS_PART = "elapsed"
        private const val STATUS_TEMPLATE = "#$STATUS_PART#"
    }
}
