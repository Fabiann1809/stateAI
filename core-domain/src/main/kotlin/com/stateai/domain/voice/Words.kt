package com.stateai.domain.voice

import java.text.Normalizer

/**
 * A spoken sentence split into words, each kept twice: as said (for display) and normalized
 * (lowercase, no accents, no punctuation) for matching.
 */
internal class Words private constructor(val shown: List<String>, val plain: List<String>) {
    val size: Int get() = plain.size

    fun drop(count: Int): Words = Words(shown.drop(count), plain.drop(count))

    fun dropLast(count: Int): Words = Words(shown.dropLast(count), plain.dropLast(count))

    fun startsWith(phrase: List<String>): Boolean = plain.size >= phrase.size && plain.subList(0, phrase.size) == phrase

    fun endsWith(phrase: List<String>): Boolean =
        plain.size >= phrase.size && plain.subList(plain.size - phrase.size, plain.size) == phrase

    fun text(): String? = shown.joinToString(" ").ifBlank { null }

    companion object {
        private val NOT_A_WORD = Regex("[^\\p{L}\\p{N}]+")
        private val DIACRITICS = Regex("\\p{Mn}+")

        fun of(sentence: String): Words {
            val shown = sentence.split(NOT_A_WORD).filter { it.isNotBlank() }
            return Words(shown, shown.map(::plain))
        }

        fun plain(word: String): String =
            Normalizer.normalize(word, Normalizer.Form.NFD).replace(DIACRITICS, "").lowercase()
    }
}

/** Normalized form of a whole phrase, split into words. */
internal fun phrase(text: String): List<String> = Words.of(text).plain
