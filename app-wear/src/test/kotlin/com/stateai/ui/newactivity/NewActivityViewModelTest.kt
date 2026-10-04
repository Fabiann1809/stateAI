package com.stateai.ui.newactivity

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.CreateActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewActivityViewModelTest {
    private val repository = InMemoryActivityRepository()
    private var nextId = 0
    private val viewModel = NewActivityViewModel(CreateActivity(repository) { ActivityId("${nextId++}") })

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `nothing is created before choosing a category`() = runTest {
        viewModel.create("BD")

        assertNull(viewModel.uiState.value.readyActivityId)
        assertEquals(0, repository.countActive())
    }

    @Test
    fun `creating after choosing a category makes the activity ready`() = runTest {
        viewModel.selectCategory(ActivityCategory.STUDY)
        viewModel.create("BD")

        assertEquals(ActivityId("0"), viewModel.uiState.value.readyActivityId)
    }

    @Test
    fun `an equivalent name reuses the existing activity`() = runTest {
        viewModel.selectCategory(ActivityCategory.STUDY)
        viewModel.create("BD")
        viewModel.create("bd ")

        assertEquals(ActivityId("0"), viewModel.uiState.value.readyActivityId)
        assertEquals(1, repository.countActive())
    }

    @Test
    fun `a voice prefill starts at the name step with the understood name`() = runTest {
        val prefilled = NewActivityViewModel(
            CreateActivity(repository) { ActivityId("${nextId++}") },
            NewActivityPrefill(ActivityCategory.STUDY, "jardinería"),
        )

        assertEquals(ActivityCategory.STUDY, prefilled.uiState.value.category)
        assertEquals("jardinería", prefilled.uiState.value.suggestedName)
    }
}
