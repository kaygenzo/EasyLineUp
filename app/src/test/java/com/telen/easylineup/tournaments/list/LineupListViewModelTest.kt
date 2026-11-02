/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineups
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [LineupListViewModel]: its use cases are mocked directly (each already has its
 * own domain-level test suite). `init {}` eagerly loads the tournament list (filter = "") and the
 * team type, so both are leniently stubbed in [setup] before construction.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LineupListViewModelTest {
    @Mock
    lateinit var getTeamUseCase: GetTeam

    @Mock
    lateinit var getAllTournamentsWithLineups: GetAllTournamentsWithLineups

    @Mock
    lateinit var getTournamentMapLink: GetTournamentMapLink

    @Mock
    lateinit var deleteTournamentLineups: DeleteTournamentLineups

    lateinit var viewModel: LineupListViewModel

    private fun tournament(id: Long, address: String? = null) = Tournament(
        id = id,
        name = "t$id",
        createdAt = 0L,
        startTime = 0L,
        endTime = 0L,
        address = address
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        runBlocking {
            Mockito.lenient().`when`(getAllTournamentsWithLineups(""))
                .thenReturn(Result.success(emptyList()))
            Mockito.lenient().`when`(getTeamUseCase())
                .thenReturn(Result.success(Team(id = 1L, name = "Panthers", type = 0)))
        }
        viewModel = LineupListViewModel(
            getTeamUseCase,
            getAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadTournamentItemsOnInit() = runTest {
        val tournament = tournament(id = 1L)
        val lineups = listOf(Lineup(id = 1L))
        runBlocking {
            Mockito.`when`(getAllTournamentsWithLineups(""))
                .thenReturn(Result.success(listOf(tournament to lineups)))
        }

        viewModel = LineupListViewModel(
            getTeamUseCase,
            getAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.tournamentItems.size)
        assertEquals(tournament, viewModel.uiState.value.tournamentItems[0].tournament)
        assertEquals(lineups, viewModel.uiState.value.tournamentItems[0].lineups)
    }

    @Test
    fun shouldUpdateTeamTypeOnInit() = runTest {
        runBlocking {
            Mockito.`when`(getTeamUseCase())
                .thenReturn(Result.success(Team(id = 1L, name = "Panthers", type = 1)))
        }

        viewModel = LineupListViewModel(
            getTeamUseCase,
            getAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        assertEquals(TeamType.getTypeById(1), viewModel.uiState.value.teamType)
    }

    @Test
    fun shouldUpdateSearchQueryAndReloadWithFilter() = runTest {
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(getAllTournamentsWithLineups("abc"))
                .thenReturn(Result.success(emptyList()))
        }

        viewModel.onSearchQueryChanged("abc")
        advanceUntilIdle()

        assertEquals("abc", viewModel.uiState.value.searchQuery)
        runBlocking { Mockito.verify(getAllTournamentsWithLineups)("abc") }
    }

    @Test
    fun shouldReloadListWhenReloadListIsCalled() = runTest {
        advanceUntilIdle()

        viewModel.reloadList()
        advanceUntilIdle()

        runBlocking { Mockito.verify(getAllTournamentsWithLineups, Mockito.times(2))("") }
    }

    @Test
    fun shouldLoadMapLinkForTournamentsWithAnAddress() = runTest {
        val tournament = tournament(id = 1L, address = "1 Main St")
        val location = GeoLocation(latitude = 1.0, longitude = 2.0)
        runBlocking {
            Mockito.`when`(getAllTournamentsWithLineups(""))
                .thenReturn(Result.success(listOf(tournament to emptyList())))
            Mockito.`when`(getTournamentMapLink(tournament)).thenReturn(Result.success(location))
        }

        viewModel = LineupListViewModel(
            getTeamUseCase,
            getAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        assertEquals(location, viewModel.uiState.value.mapsByTournamentId[1L])
    }

    @Test
    fun shouldDeleteTournamentAndReloadOnSuccess() = runTest {
        advanceUntilIdle()
        val tournament = tournament(id = 5L)
        runBlocking {
            Mockito.`when`(deleteTournamentLineups(tournament)).thenReturn(Result.success(Unit))
        }

        val result = viewModel.deleteTournament(tournament)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        runBlocking { Mockito.verify(getAllTournamentsWithLineups, Mockito.times(2))("") }
    }

    @Test
    fun shouldPropagateFailureWhenDeleteTournamentFails() = runTest {
        advanceUntilIdle()
        val tournament = tournament(id = 5L)
        val exception = Exception("db error")
        runBlocking {
            Mockito.`when`(deleteTournamentLineups(tournament))
                .thenReturn(Result.failure(exception))
        }

        val result = viewModel.deleteTournament(tournament)

        assertTrue(result.isFailure)
    }
}
