package com.stateai.data.mascot

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stateai.domain.mascot.MascotName
import com.stateai.domain.mascot.MascotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Stores the mascot's name in a Preferences DataStore. An absent key means "not asked yet". */
class DataStoreMascotRepository(private val dataStore: DataStore<Preferences>) : MascotRepository {
    override fun observeName(): Flow<String?> = dataStore.data.map { it[NAME] }

    override suspend fun saveName(name: String) {
        dataStore.edit { it[NAME] = MascotName.clean(name) }
    }

    private companion object {
        val NAME = stringPreferencesKey("mascot_name")
    }
}
