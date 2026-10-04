package com.stateai.ui.voice

import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId

/** Where the voice flow can end: a session, "Nueva actividad" prefilled, or back to the list. */
class VoiceExits(
    val onStart: (ActivityId) -> Unit,
    val onReview: (ActivityCategory?, String?) -> Unit,
    val onFallback: () -> Unit,
)
