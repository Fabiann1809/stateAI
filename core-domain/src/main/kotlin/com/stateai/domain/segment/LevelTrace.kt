package com.stateai.domain.segment

import com.stateai.domain.state.ActivationLevel

/**
 * Per-window trace of a segment: one symbol per feature window (about one per minute, the first one
 * minute after the start). `L`, `M`, `H` are the estimated levels; `-` marks a window without a
 * usable estimate (not clean, or still calibrating). It is a derived summary, not the raw signal.
 */
@JvmInline
value class LevelTrace(val symbols: String = "") {
    init {
        require(symbols.all { it in ALPHABET }) { "Unknown trace symbol in '$symbols'" }
    }

    val windowCount: Int get() = symbols.length
    val cleanWindowCount: Int get() = symbols.count { it != UNUSABLE }

    /** Level of each window, or null when the window had no usable estimate. */
    fun levels(): List<ActivationLevel?> = symbols.map { symbol ->
        ActivationLevel.entries.firstOrNull { it.name.first() == symbol }
    }

    fun plus(level: ActivationLevel?): LevelTrace = LevelTrace(symbols + (level?.name?.first() ?: UNUSABLE))

    companion object {
        const val UNUSABLE = '-'
        private const val ALPHABET = "LMH-"
    }
}
