package com.stateai.ui.common

import android.app.RemoteInput
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import kotlinx.coroutines.CompletableDeferred

private const val TEXT_KEY = "text"

/** Returns a function that opens the standard Wear OS text input (keyboard or voice). */
@Composable
fun rememberTextInput(label: String, onText: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        textOf(result)?.let(onText)
    }
    return { launcher.launch(textInputIntent(label)) }
}

/**
 * Same input as a suspending call: opens it and returns the text, or null when it was dismissed.
 * Used as the typed stand-in for voice in debug builds.
 */
@Composable
fun rememberSuspendingTextInput(label: String): suspend () -> String? {
    val pending = remember { arrayOfNulls<CompletableDeferred<String?>>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        pending[0]?.complete(textOf(result))
    }
    return {
        val answer = CompletableDeferred<String?>().also { pending[0] = it }
        launcher.launch(textInputIntent(label))
        answer.await()
    }
}

private fun textOf(result: ActivityResult): String? =
    result.data?.let { RemoteInput.getResultsFromIntent(it)?.getCharSequence(TEXT_KEY) }?.toString()

private fun textInputIntent(label: String): Intent {
    val remoteInput = RemoteInput.Builder(TEXT_KEY)
        .setLabel(label)
        .wearableExtender {
            setEmojisAllowed(false)
            setInputActionType(EditorInfo.IME_ACTION_DONE)
        }
        .build()
    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
    RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
    return intent
}
