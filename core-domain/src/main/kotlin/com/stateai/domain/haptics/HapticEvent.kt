package com.stateai.domain.haptics

/** Haptic language of the app (SPEC 6.2). */
enum class HapticEvent {
    /** Start of a block or of a focus window: one short pulse. */
    BLOCK_START,

    /** Breathing guide: slow pulses at inhale 4 s / exhale 6 s. */
    BREATHE,

    /** Suggested pause: one long, soft pulse. */
    PAUSE_SUGGESTED,

    /** Overload alert: two short pulses. */
    OVERLOAD_ALERT,
    ;

    /**
     * Start events and the breathing guide (which the user starts on purpose) are not
     * interruptions, so the rate limits do not apply to them.
     */
    val isExemptFromRateLimit: Boolean get() = this == BLOCK_START || this == BREATHE
}
