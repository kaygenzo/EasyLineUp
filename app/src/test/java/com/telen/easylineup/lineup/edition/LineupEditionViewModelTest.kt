/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.SaveLineupChanges
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [LineupEditionViewModel]: its use cases are mocked directly (each already has
 * its own domain-level test suite), so this only verifies the ViewModel's own orchestration.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LineupEditionViewModelTest {
    @Mock
    lateinit var getLineupById: GetLineupById

    @Mock
    lateinit var getRoster: GetRoster

    @Mock
    lateinit var saveLineupChanges: SaveLineupChanges

    @Mock
    lateinit var observeTournaments: ObserveTournaments

    lateinit var viewModel: LineupEditionViewModel
    private val tournament =
        Tournament(id = 1L, name = "Summer cup", createdAt = 0L, startTime = 0L, endTime = 0L)
    private val lineup = Lineup(id = 1L, name = "Game 1", teamId = 1L, tournamentId = 1L)
    private val roster = TeamRosterSummary(status = 0, players = emptyList())

    @Before
    fun setup() {
        // init {} subscribes to observeTournaments() eagerly via viewModelScope.launch, which
        // needs Dispatchers.Main set up - and the Flow stubbed - before construction.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Mockito.`when`(observeTournaments()).thenReturn(flowOf(listOf(tournament)))

        viewModel = LineupEditionViewModel(
            getLineupById,
            getRoster,
            saveLineupChanges,
            observeTournaments
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldExposeTournamentsFromObserveTournaments() = runTest {
        advanceUntilIdle()

        assertEquals(listOf(tournament), viewModel.uiState.value.tournaments)
    }

    @Test
    fun shouldReturnLineupAndRosterOnLoadSuccess() = runTest {
        runBlocking {
            Mockito.`when`(getLineupById(1L)).thenReturn(Result.success(lineup))
            Mockito.`when`(getRoster(1L)).thenReturn(Result.success(roster))
        }

        val result = viewModel.load(1L)

        assertTrue(result.isSuccess)
        assertEquals(lineup to roster, result.getOrNull())
    }

    @Test
    fun shouldFailLoadWhenGetLineupByIdFails() = runTest {
        val exception = Exception("not found")
        runBlocking {
            Mockito.`when`(getLineupById(1L)).thenReturn(Result.failure(exception))
        }

        val result = viewModel.load(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldSendLineupUpdatedWhenSaveSucceeds() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup, roster.players))
                .thenReturn(Result.success(Unit))
        }

        val events = mutableListOf<LineupEditionEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.save(lineup, roster)
        advanceUntilIdle()

        assertEquals(listOf(LineupUpdated), events)
        job.cancel()
    }

    @Test
    fun shouldSendInvalidLineupNameWhenUpdateLineupFailsWithEmptyName() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup, roster.players))
                .thenReturn(Result.failure(LineupNameEmptyException()))
        }

        val events = mutableListOf<LineupEditionEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.save(lineup, roster)
        advanceUntilIdle()

        assertEquals(listOf(InvalidLineupName), events)
        job.cancel()
    }

    @Test
    fun shouldSendInvalidTournamentWhenUpdateLineupFailsWithEmptyTournament() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup, roster.players))
                .thenReturn(Result.failure(TournamentNameEmptyException()))
        }

        val events = mutableListOf<LineupEditionEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.save(lineup, roster)
        advanceUntilIdle()

        assertEquals(listOf(InvalidTournament), events)
        job.cancel()
    }

    @Test
    fun shouldReportAnUnrelatedFailure() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup, roster.players))
                .thenReturn(Result.failure(Exception("db error")))
        }

        val events = mutableListOf<LineupEditionEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.save(lineup, roster)
        advanceUntilIdle()

        assertEquals(listOf(LineupUpdateFailed), events)
        job.cancel()
    }
}
