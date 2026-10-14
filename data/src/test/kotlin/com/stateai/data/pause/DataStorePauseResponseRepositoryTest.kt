package com.stateai.data.pause

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.stateai.domain.pause.PauseResponses
import com.stateai.domain.session.SuggestionReason
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStorePauseResponseRepositoryTest {
    @TempDir
    lateinit var directory: File

    @Test
    fun `nothing is stored until a suggestion is answered`() = runTest {
        assertTrue(repository().load().isEmpty())
    }

    @Test
    fun `the three counters of a reason are loaded back and the other reasons stay absent`() = runTest {
        val repository = repository()

        repository.save(SuggestionReason.RESTLESS, PauseResponses(shown = 6, accepted = 1, skipped = 2))

        val loaded = repository.load()
        assertEquals(mapOf(SuggestionReason.RESTLESS to PauseResponses(shown = 6, accepted = 1, skipped = 2)), loaded)
    }

    private fun TestScope.repository() = DataStorePauseResponseRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(directory, "pause.preferences_pb") },
    )
}
