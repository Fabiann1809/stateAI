package com.stateai.domain.voice

import com.stateai.domain.activity.ActivityCategory

/**
 * Turns a spoken sentence into a [VoiceIntent], in Spanish and without any network or model:
 * greetings (and the mascot's name) and lead-ins like "voy a" are skipped, the first verb gives the
 * category, and what follows (without linking words and time fillers) is the activity's name.
 *
 * "hola Lumi, voy a estudiar bases de datos" -> STUDY, "bases de datos".
 */
class IntentParser(mascotName: String? = null) {
    private val greetings =
        IntentVocabulary.greetings + listOfNotNull(mascotName?.let(::phrase)?.takeIf { it.isNotEmpty() })

    fun parse(sentence: String): VoiceIntent {
        var words = skipAll(Words.of(sentence), greetings + IntentVocabulary.leadIns)
        if (words.size == 0) return VoiceIntent.EMPTY

        val verb = IntentVocabulary.verbs[words.plain.first()]
        if (verb != null) words = words.drop(1)
        val name = nameIn(words)
        val noun = name?.let { IntentVocabulary.nouns[phrase(it).first()] }
        val specificVerb = verb?.takeIf { it != ActivityCategory.OTHER }
        val category = specificVerb ?: noun ?: verb
        return VoiceIntent(category, name, confidenceOf(specificVerb, noun, verb, name))
    }

    /** Fillers go first ("un rato" must not lose its "un" as a linking word), then linking words. */
    private fun nameIn(afterVerb: Words): String? {
        var words = skipAll(skipTrailing(afterVerb), IntentVocabulary.quantities)
        while (words.size > 0 && words.plain.first() in IntentVocabulary.connectors) words = words.drop(1)
        return skipTrailing(words).text()
    }

    private fun skipAll(words: Words, phrases: List<List<String>>): Words {
        var current = words
        while (true) {
            val match = phrases.firstOrNull { current.startsWith(it) } ?: return current
            current = current.drop(match.size)
        }
    }

    private fun skipTrailing(words: Words): Words {
        var current = words
        while (true) {
            val filler = IntentVocabulary.fillers.firstOrNull { current.endsWith(it) }
            val connector = current.plain.lastOrNull()?.takeIf { it in IntentVocabulary.connectors }
            current = when {
                filler != null -> current.dropLast(filler.size)
                connector != null -> current.dropLast(1)
                else -> return current
            }
        }
    }

    private fun confidenceOf(
        specificVerb: ActivityCategory?,
        noun: ActivityCategory?,
        anyVerb: ActivityCategory?,
        name: String?,
    ): Double = when {
        specificVerb != null -> if (name != null) VERB_AND_NAME else VERB_ONLY
        noun != null -> NOUN
        anyVerb != null && name != null -> GENERIC_VERB_AND_NAME
        anyVerb == null && name != null -> NAME_ONLY
        else -> NOTHING
    }

    private companion object {
        const val VERB_AND_NAME = 0.9
        const val VERB_ONLY = 0.8
        const val NOUN = 0.7
        const val GENERIC_VERB_AND_NAME = 0.6
        const val NAME_ONLY = 0.4
        const val NOTHING = 0.0
    }
}
