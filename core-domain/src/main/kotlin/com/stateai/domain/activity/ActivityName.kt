package com.stateai.domain.activity

import java.text.Normalizer

/**
 * Free activity name in normalized form: lowercase, without accents and with single spaces.
 * Two raw names that normalize to the same value refer to the same activity ("BD" and "bd ").
 */
@JvmInline
value class ActivityName private constructor(val value: String) {
    companion object {
        private val DIACRITICS = Regex("\\p{Mn}+")
        private val WHITESPACE = Regex("\\s+")

        /** Returns the normalized name, or null when the raw text has no visible characters. */
        fun of(raw: String): ActivityName? {
            val normalized = normalize(raw)
            return if (normalized.isEmpty()) null else ActivityName(normalized)
        }

        private fun normalize(raw: String): String = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .replace(WHITESPACE, " ")
            .trim()
            .lowercase()
    }
}
