package com.stateai.domain.activity

import java.text.Normalizer

/**
 * Free activity name. [normalized] (lowercase, no accents, single spaces) is the identity:
 * two names with the same normalized form are the same activity ("BD" and "bd ").
 * [display] keeps the text as first typed, trimmed, for the UI.
 */
class ActivityName private constructor(val normalized: String, val display: String) {
    override fun equals(other: Any?): Boolean = other is ActivityName && other.normalized == normalized

    override fun hashCode(): Int = normalized.hashCode()

    override fun toString(): String = display

    companion object {
        private val DIACRITICS = Regex("\\p{Mn}+")
        private val WHITESPACE = Regex("\\s+")

        /** Returns the name, or null when the raw text has no visible characters. */
        fun of(raw: String): ActivityName? {
            val display = raw.replace(WHITESPACE, " ").trim()
            if (display.isEmpty()) return null
            return ActivityName(normalize(display), display)
        }

        private fun normalize(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()
    }
}
