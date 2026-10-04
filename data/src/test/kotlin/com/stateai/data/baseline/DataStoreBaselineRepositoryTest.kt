package com.stateai.data.baseline

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.stateai.domain.baseline.UserBaseline
import java.io.File
import java.time.Instant
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStoreBaselineRepositoryTest {
    @TempDir
    lateinit var directory: File

    private val baseline = UserBaseline(64.5, 1.2, Instant.parse("2026-10-04T09:02:00Z"))

    @Test
    fun `has no baseline before calibration`() = runTest {
        assertNull(repository().load())
    }

    @Test
    fun `saved baseline is loaded back`() = runTest {
        val repository = repository()
        repository.save(baseline)

        assertEquals(baseline, repository.load())
    }

    private fun TestScope.repository() = DataStoreBaselineRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(directory, "baseline.preferences_pb") },
    )
}
