package com.stateai.domain.haptics

import com.stateai.domain.session.SuggestionReason

/** A vibration the session plays and why, so the screen can show the matching suggestion. */
data class SessionCue(val event: HapticEvent, val reason: SuggestionReason)
