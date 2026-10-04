package com.stateai.data.voice

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStoreVoiceConsentRepositoryTest {
    @TempDir
    lateinit var directory: File

    @Test
    fun `no consent until it is given`() = runTest {
        assertFalse(repository().hasConsented())
    }

    @Test
    fun `consent is remembered`() = runTest {
        val repository = repository()

        repository.consent()

        assertTrue(repository.hasConsented())
    }

    private fun TestScope.repository() = DataStoreVoiceConsentRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(directory, "voice.preferences_pb") },
    )
}
