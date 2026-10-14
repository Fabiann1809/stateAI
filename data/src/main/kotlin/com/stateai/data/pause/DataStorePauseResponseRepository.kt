package com.stateai.data.pause

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.stateai.domain.pause.PauseResponseRepository
import com.stateai.domain.pause.PauseResponses
import com.stateai.domain.session.SuggestionReason
import kotlinx.coroutines.flow.first

/** Stores the responses to pause suggestions in a Preferences DataStore, three counters per reason. */
class DataStorePauseResponseRepository(private val dataStore: DataStore<Preferences>) : PauseResponseRepository {
    override suspend fun load(): Map<SuggestionReason, PauseResponses> {
        val stored = dataStore.data.first()
        return SuggestionReason.entries.mapNotNull { reason ->
            val shown = stored[key(reason, SHOWN)] ?: return@mapNotNull null
            reason to PauseResponses(
                shown = shown,
                accepted = stored[key(reason, ACCEPTED)] ?: 0,
                skipped = stored[key(reason, SKIPPED)] ?: 0,
            )
        }.toMap()
    }

    override suspend fun save(reason: SuggestionReason, responses: PauseResponses) {
        dataStore.edit {
            it[key(reason, SHOWN)] = responses.shown
            it[key(reason, ACCEPTED)] = responses.accepted
            it[key(reason, SKIPPED)] = responses.skipped
        }
    }

    private fun key(reason: SuggestionReason, counter: String) =
        intPreferencesKey("${reason.name.lowercase()}_$counter")

    private companion object {
        const val SHOWN = "shown"
        const val ACCEPTED = "accepted"
        const val SKIPPED = "skipped"
    }
}
