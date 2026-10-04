package com.stateai.domain.voice

import com.stateai.domain.activity.ActivityCategory

/**
 * What the person said they will do. [category] is null when no activity verb was recognized;
 * [name] is null when they named no specific activity ("voy a leer un rato").
 * [confidence] goes from 0 (nothing understood) to 1 (verb and name clearly found).
 */
data class VoiceIntent(val category: ActivityCategory?, val name: String?, val confidence: Double) {
    val isEmpty: Boolean get() = category == null && name == null

    companion object {
        val EMPTY = VoiceIntent(category = null, name = null, confidence = 0.0)
    }
}
