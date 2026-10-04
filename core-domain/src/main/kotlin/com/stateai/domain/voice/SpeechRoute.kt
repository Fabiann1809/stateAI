package com.stateai.domain.voice

/** How the next spoken request will be captured. */
enum class SpeechRoute {
    /** Recognizer running on the watch: no audio leaves it. */
    ON_DEVICE,

    /** The system recognizer, which may send audio to a server; only with the person's consent. */
    SYSTEM,

    /** The system recognizer is the only option and consent is still missing: ask first. */
    ASK_CONSENT,

    /** Typed text instead of voice: debug builds and the emulator, which has no recognizer. */
    TEXT,

    /** No way to listen: fall back to the activity list. */
    UNAVAILABLE,
    ;

    /** Voice routes need the microphone permission; text does not. */
    val needsMicrophone: Boolean get() = this == ON_DEVICE || this == SYSTEM

    companion object {
        /** The private option first; the system recognizer only with consent; text only for debugging. */
        fun choose(onDevice: Boolean, system: Boolean, consented: Boolean, allowText: Boolean): SpeechRoute = when {
            onDevice -> ON_DEVICE
            system && consented -> SYSTEM
            system -> ASK_CONSENT
            allowText -> TEXT
            else -> UNAVAILABLE
        }
    }
}

/** Why a spoken request did not lead to a session. Every case falls back to the activity list. */
enum class VoiceFailure {
    NO_PERMISSION,
    NO_RECOGNIZER,
    NO_NETWORK,
    SILENCE,
    NOT_UNDERSTOOD,
    NO_CONSENT,
    ACTIVITY_LIMIT,
    ERROR,
}
