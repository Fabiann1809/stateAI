package com.stateai.ui.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

fun Context.hasMicrophonePermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

/** Returns a function that asks for the microphone and reports whether it was granted. */
@Composable
fun rememberMicrophonePermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return { launcher.launch(Manifest.permission.RECORD_AUDIO) }
}
