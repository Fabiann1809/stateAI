package com.stateai.domain.activity

/**
 * How alike two activity names are, from 0 to 1, on their normalized form (lowercase, no accents):
 * identical names score 1, small typos score high ("baces de datos"), and a name whose words are all
 * part of the other ("informe" in "informe trimestral") scores [CONTAINED]. Shared by voice matching
 * and, later, the "is it the same activity?" question when creating one.
 */
object NameSimilarity {
    const val SIMILAR = 0.8
    private const val CONTAINED = 0.85
    private const val MIN_CONTAINED_LENGTH = 4

    fun score(first: ActivityName, second: ActivityName): Double {
        val a = first.normalized
        val b = second.normalized
        if (a == b) return 1.0
        val edits = 1.0 - levenshtein(a, b).toDouble() / maxOf(a.length, b.length)
        return maxOf(edits, containment(a, b))
    }

    fun areSimilar(first: ActivityName, second: ActivityName): Boolean = score(first, second) >= SIMILAR

    private fun containment(a: String, b: String): Double {
        val (short, long) = if (a.length <= b.length) a to b else b to a
        val shortWords = short.split(' ')
        val longWords = long.split(' ').toSet()
        val contained = short.length >= MIN_CONTAINED_LENGTH && shortWords.all { it in longWords }
        return if (contained) CONTAINED else 0.0
    }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val substitution = previous[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(previous[j] + 1, current[j - 1] + 1, substitution)
            }
            previous = current
        }
        return previous[b.length]
    }
}
