package com.stateai.data.memory

import com.stateai.domain.voice.VoiceConsentRepository

/** Voice consent kept in memory, for tests. */
class InMemoryVoiceConsentRepository(private var consented: Boolean = false) : VoiceConsentRepository {
    override suspend fun hasConsented(): Boolean = consented

    override suspend fun consent() {
        consented = true
    }
}
