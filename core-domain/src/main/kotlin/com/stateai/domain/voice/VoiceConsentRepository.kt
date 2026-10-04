package com.stateai.domain.voice

/**
 * Whether the person accepted that the system recognizer may send their voice to a server.
 * Only the acceptance is stored; declining leaves it unset, so tapping the mascot asks again.
 */
interface VoiceConsentRepository {
    suspend fun hasConsented(): Boolean

    suspend fun consent()
}
