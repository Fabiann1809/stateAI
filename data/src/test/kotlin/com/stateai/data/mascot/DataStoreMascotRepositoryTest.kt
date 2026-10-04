package com.stateai.data.mascot

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStoreMascotRepositoryTest {
    @TempDir
    lateinit var directory: File

    @Test
    fun `no name until the person is asked`() = runTest {
        assertNull(repository().observeName().first())
    }

    @Test
    fun `stores the cleaned name`() = runTest {
        val repository = repository()

        repository.saveName("  Lumi ")

        assertEquals("Lumi", repository.observeName().first())
    }

    @Test
    fun `skipping stores an empty name, so it is not asked again`() = runTest {
        val repository = repository()

        repository.saveName("")

        assertEquals("", repository.observeName().first())
    }

    private fun TestScope.repository() = DataStoreMascotRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(directory, "mascot.preferences_pb") },
    )
}
