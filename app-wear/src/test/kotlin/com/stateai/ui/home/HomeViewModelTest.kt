package com.stateai.ui.home

import com.stateai.data.memory.InMemoryActivityRepository
import com.stateai.data.memory.InMemoryMascotRepository
import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityLimits
import com.stateai.domain.activity.ActivityName
import com.stateai.domain.energy.EnergyBudget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class HomeViewModelTest {
    private val repository = InMemoryActivityRepository()
    private val mascot = InMemoryMascotRepository()

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
        val viewModel = HomeViewModel(repository, flowOf(null), flowOf(false), emptyFlow(), mascot)
        viewModel.uiState.launchIn(backgroundScope)

        val state = viewModel.uiState.first { it.activities.isNotEmpty() }
        assertEquals(2, state.activities.size)
        assertTrue(state.canCreateNew)
    }

    @Test
    fun `disables new activity when the limit is reached`() = runTest {
        addActivities(count = ActivityLimits.MAX_ACTIVE)
        val viewModel = HomeViewModel(repository, flowOf(null), flowOf(false), emptyFlow(), mascot)
        viewModel.uiState.launchIn(backgroundScope)

        val state = viewModel.uiState.first { it.activities.isNotEmpty() }
        assertFalse(state.canCreateNew)
    }

    @Test
    fun `shows the focus window hint`() = runTest {
        val viewModel = HomeViewModel(repository, flowOf(null), flowOf(true), emptyFlow(), mascot)
        viewModel.uiState.launchIn(backgroundScope)

        assertTrue(viewModel.uiState.first { it.isFocusWindow }.isFocusWindow)
    }

    @Test
    fun `puts the suggested activity first`() = runTest {
        addActivities(count = 3)
        val viewModel = HomeViewModel(repository, flowOf(ActivityId("2")), flowOf(false), emptyFlow(), mascot)
        viewModel.uiState.launchIn(backgroundScope)

        val state = viewModel.uiState.first { it.suggested != null }
        assertEquals(listOf("2", "0", "1"), state.activities.map { it.id.value })
    }

    @Test
    fun `shows today's estimated energy`() = runTest {
        val energy = EnergyBudget(level = 64.0, consumed = 40.0, recovered = 4.0, isLow = false)
        val viewModel = HomeViewModel(repository, flowOf(null), flowOf(false), flowOf(energy), mascot)
        viewModel.uiState.launchIn(backgroundScope)

        assertEquals(energy, viewModel.uiState.first { it.energy != null }.energy)
    }

    @Test
    fun `asks for the mascot name once and remembers the answer`() = runTest {
        val viewModel = HomeViewModel(repository, flowOf(null), flowOf(false), emptyFlow(), mascot)
        viewModel.uiState.launchIn(backgroundScope)

        assertEquals(
            MascotNameState.NotAsked,
            viewModel.uiState.first {
                it.mascotName != MascotNameState.Loading
            }.mascotName,
        )
        viewModel.nameMascot(" Lumi ")
        assertEquals(
            MascotNameState.Known("Lumi"),
            viewModel.uiState.first {
                it.mascotName is MascotNameState.Known
            }.mascotName,
        )
    }

    private suspend fun addActivities(count: Int) {
        repeat(count) { index ->
            repository.add(Activity(ActivityId("$index"), ActivityCategory.STUDY, ActivityName.of("subject $index")))
        }
    }
}
