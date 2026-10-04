package com.stateai.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.stateai.BuildConfig

private const val READ_HEART_RATE = "android.permission.health.READ_HEART_RATE"

/**
 * Asks once for the permissions a session uses: notifications (to show the ongoing session) and,
 * with real sensors, heart rate. The session runs either way.
 */
@Composable
fun RequestSessionPermissions() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        val missing = sessionPermissions().filterNot { context.isGranted(it) }
        if (missing.isNotEmpty()) launcher.launch(missing.toTypedArray())
    }
}

private fun sessionPermissions(): List<String> = buildList {
    add(Manifest.permission.POST_NOTIFICATIONS)
    if (BuildConfig.USE_HEALTH_SERVICES) {
        add(
            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.BAKLAVA
            ) {
                READ_HEART_RATE
            } else {
                Manifest.permission.BODY_SENSORS
            },
        )
    }
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
