package com.stateai.ui.picker

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PickerViewModelTest {
    private val repository = InMemoryActivityRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows active activities and allows creating more below the limit`() = runTest {
        addActivities(count = 2)
        val viewModel = PickerViewModel(repository)
        viewModel.uiState.launchIn(backgroundScope)

        val state = viewModel.uiState.first { it.activities.isNotEmpty() }
        assertEquals(2, state.activities.size)
        assertTrue(state.canCreateNew)
    }

    @Test
    fun `disables new activity when the limit is reached`() = runTest {
        addActivities(count = ActivityLimits.MAX_ACTIVE)
        val viewModel = PickerViewModel(repository)
        viewModel.uiState.launchIn(backgroundScope)

        val state = viewModel.uiState.first { it.activities.isNotEmpty() }
        assertFalse(state.canCreateNew)
    }

    private suspend fun addActivities(count: Int) {
        repeat(count) { index ->
            repository.add(Activity(ActivityId("$index"), ActivityCategory.STUDY, ActivityName.of("subject $index")))
        }
    }
}
