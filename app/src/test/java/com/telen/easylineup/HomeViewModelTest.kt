/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.lifecycle.SavedStateHandle
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [HomeViewModel]: its two use cases are mocked directly (each already has its
 * own domain-level test suite), so this only verifies the ViewModel's own orchestration -
 * forwarding the right [Event] on success/failure, and
 * opening/closing the swap-team sheet through [HomeUiState].
 */
@RunWith(MockitoJUnitRunner::class)
internal class HomeViewModelTest {
    @Mock
    lateinit var getAllTeams: GetAllTeams

    @Mock
    lateinit var saveCurrentTeam: SaveCurrentTeam

    lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = HomeViewModel(getAllTeams, saveCurrentTeam, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldOpenTheSwapSheetWithTheFetchedTeams() = runTest {
        val teams = listOf(Team(id = 1L, name = "Panthers", main = true))
        runBlocking { Mockito.`when`(getAllTeams()).thenReturn(Result.success(teams)) }

        viewModel.onSwapButtonClicked()
        advanceUntilIdle()

        assertEquals(teams, viewModel.uiState.value.swapTeams)
    }

    @Test
    fun dismissingTheSwapSheetClosesIt() = runTest {
        val teams = listOf(Team(id = 1L, name = "Panthers", main = true))
        runBlocking { Mockito.`when`(getAllTeams()).thenReturn(Result.success(teams)) }
        viewModel.onSwapButtonClicked()
        advanceUntilIdle()

        viewModel.onSwapTeamDismiss()

        assertNull(viewModel.uiState.value.swapTeams)
    }

    @Test
    fun anOpenSwapSheetIsReopenedWithFreshTeamsAfterProcessDeath() = runTest {
        val teams = listOf(Team(id = 1L, name = "Panthers", main = true))
        runBlocking { Mockito.`when`(getAllTeams()).thenReturn(Result.success(teams)) }
        val saved = SavedStateHandle()
        HomeViewModel(getAllTeams, saveCurrentTeam, saved).onSwapButtonClicked()
        advanceUntilIdle()

        val restored = HomeViewModel(getAllTeams, saveCurrentTeam, saved)
        advanceUntilIdle()

        assertEquals(teams, restored.uiState.value.swapTeams)
    }

    @Test
    fun shouldSendSwapButtonFailureWhenFetchFails() = runTest {
        val exception = Exception("db error")
        runBlocking { Mockito.`when`(getAllTeams()).thenReturn(Result.failure(exception)) }

        val events = mutableListOf<Event>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onSwapButtonClicked()
        advanceUntilIdle()

        assertEquals(listOf(SwapButtonFailure), events)
        job.cancel()
    }

    @Test
    fun shouldSendUpdateCurrentTeamSuccessWhenSaveSucceeds() = runTest {
        val team = Team(id = 2L, name = "Lions", main = false)
        runBlocking { Mockito.`when`(saveCurrentTeam(team)).thenReturn(Result.success(Unit)) }

        val events = mutableListOf<Event>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.updateCurrentTeam(team)
        advanceUntilIdle()

        assertEquals(listOf(UpdateCurrentTeamSuccess), events)
        job.cancel()
    }

    @Test
    fun shouldSendUpdateCurrentTeamFailureWhenSaveFails() = runTest {
        val team = Team(id = 2L, name = "Lions", main = false)
        val exception = Exception("db error")
        runBlocking { Mockito.`when`(saveCurrentTeam(team)).thenReturn(Result.failure(exception)) }

        val events = mutableListOf<Event>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.updateCurrentTeam(team)
        advanceUntilIdle()

        assertEquals(listOf(UpdateCurrentTeamFailure), events)
        job.cancel()
    }
}
