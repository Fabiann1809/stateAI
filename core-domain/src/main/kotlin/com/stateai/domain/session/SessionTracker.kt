package com.stateai.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds the single session that can be running at a time. */
class SessionTracker {
    private val current = MutableStateFlow<ActiveSession?>(null)

    val activeSession: StateFlow<ActiveSession?> = current.asStateFlow()

    fun start(session: ActiveSession) {
        current.value = session
    }

    fun stop() {
        current.value = null
    }
}
