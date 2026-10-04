package com.stateai.domain.mascot

import kotlinx.coroutines.flow.Flow

/** Storage of the mascot's name, chosen once on first use. */
interface MascotRepository {
    /** Null until the person has been asked; empty when they chose not to name it. */
    fun observeName(): Flow<String?>

    suspend fun saveName(name: String)
}

/** Names are trimmed, single-spaced and at most [MAX_LENGTH] characters, so they fit on the watch. */
object MascotName {
    const val MAX_LENGTH = 16
    private val WHITESPACE = Regex("\\s+")

    fun clean(raw: String): String = raw.replace(WHITESPACE, " ").trim().take(MAX_LENGTH).trim()
}
