package com.stateai.data.voice

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.stateai.domain.voice.VoiceConsentRepository
import kotlinx.coroutines.flow.first

/** Stores the system recognizer consent in a Preferences DataStore. */
class DataStoreVoiceConsentRepository(private val dataStore: DataStore<Preferences>) : VoiceConsentRepository {
    override suspend fun hasConsented(): Boolean = dataStore.data.first()[SYSTEM_RECOGNIZER] ?: false

    override suspend fun consent() {
        dataStore.edit { it[SYSTEM_RECOGNIZER] = true }
    }

    private companion object {
        val SYSTEM_RECOGNIZER = booleanPreferencesKey("system_recognizer_consent")
    }
}
