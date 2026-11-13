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
import com.telen.easylineup.domain.usecases.ObserveCurrentTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.ObserveAllTournamentsWithLineups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
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
    lateinit var observeCurrentTeam: ObserveCurrentTeam

    @Mock
    lateinit var observeAllTournamentsWithLineups: ObserveAllTournamentsWithLineups

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
            Mockito.lenient().`when`(observeAllTournamentsWithLineups(""))
                .thenReturn(flowOf(emptyList()))
            Mockito.lenient().`when`(observeCurrentTeam())
                .thenReturn(flowOf(Team(id = 1L, name = "Panthers", type = 0, main = true)))
        }
        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
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
            Mockito.`when`(observeAllTournamentsWithLineups(""))
                .thenReturn(flowOf(listOf(tournament to lineups)))
        }

        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
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
    fun shouldReportALoadFailureInsteadOfAnEmptyList() = runTest {
        runBlocking {
            Mockito.`when`(observeAllTournamentsWithLineups(""))
                .thenReturn(flow { throw IllegalStateException("db error") })
        }

        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadFailed)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun shouldReadTheTournamentsAgainOnRetry() = runTest {
        val tournament = tournament(id = 1L)
        runBlocking {
            Mockito.`when`(observeAllTournamentsWithLineups(""))
                .thenReturn(flow { throw IllegalStateException("db error") })
                .thenReturn(flowOf(listOf(tournament to listOf(Lineup(id = 1L)))))
        }
        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        viewModel.onRetryLoad()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loadFailed)
        assertEquals(tournament, viewModel.uiState.value.tournamentItems[0].tournament)
    }

    @Test
    fun shouldUpdateTeamTypeOnInit() = runTest {
        runBlocking {
            Mockito.`when`(observeCurrentTeam())
                .thenReturn(flowOf(Team(id = 1L, name = "Panthers", type = 1, main = true)))
        }

        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
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
            Mockito.`when`(observeAllTournamentsWithLineups("abc"))
                .thenReturn(flowOf(emptyList()))
        }

        viewModel.onSearchQueryChanged("abc")
        advanceUntilIdle()

        assertEquals("abc", viewModel.uiState.value.searchQuery)
        Mockito.verify(observeAllTournamentsWithLineups)("abc")
    }

    @Test
    fun shouldFollowEveryEmissionOfTheObservedList() = runTest {
        val source = MutableStateFlow<List<Pair<Tournament, List<Lineup>>>>(emptyList())
        Mockito.`when`(observeAllTournamentsWithLineups("")).thenReturn(source)
        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.tournamentItems.size)

        // e.g. a lineup saved on the creation screen, with no reload asked for
        source.value = listOf(tournament(id = 1L) to listOf(Lineup(id = 7L)))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.tournamentItems.size)
        assertEquals(7L, viewModel.uiState.value.tournamentItems[0].lineups.single().id)
    }

    @Test
    fun shouldNotLookUpTheSameAddressAgainOnLaterEmissions() = runTest {
        val tournament = tournament(id = 1L, address = "1 Main St")
        val location = GeoLocation(latitude = 1.0, longitude = 2.0)
        val source = MutableStateFlow(listOf(tournament to emptyList<Lineup>()))
        runBlocking {
            Mockito.`when`(observeAllTournamentsWithLineups("")).thenReturn(source)
            Mockito.`when`(getTournamentMapLink(tournament)).thenReturn(Result.success(location))
        }
        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        source.value = listOf(tournament to listOf(Lineup(id = 3L)))
        advanceUntilIdle()

        runBlocking { Mockito.verify(getTournamentMapLink, Mockito.times(1))(tournament) }
    }

    @Test
    fun shouldLoadMapLinkForTournamentsWithAnAddress() = runTest {
        val tournament = tournament(id = 1L, address = "1 Main St")
        val location = GeoLocation(latitude = 1.0, longitude = 2.0)
        runBlocking {
            Mockito.`when`(observeAllTournamentsWithLineups(""))
                .thenReturn(flowOf(listOf(tournament to emptyList())))
            Mockito.`when`(getTournamentMapLink(tournament)).thenReturn(Result.success(location))
        }

        viewModel = LineupListViewModel(
            observeCurrentTeam,
            observeAllTournamentsWithLineups,
            getTournamentMapLink,
            deleteTournamentLineups
        )
        advanceUntilIdle()

        assertEquals(location, viewModel.uiState.value.mapsByTournamentId[1L])
    }

    /** Starts collecting [flow] on the test scheduler, for the rest of the test. */
    private fun <T> TestScope.collectEvents(flow: Flow<T>): List<T> {
        val events = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(events) }
        return events
    }

    @Test
    fun shouldDeleteTournament() = runTest {
        advanceUntilIdle()
        val tournament = tournament(id = 5L)
        runBlocking {
            Mockito.`when`(deleteTournamentLineups(tournament)).thenReturn(Result.success(Unit))
        }

        val events = collectEvents(viewModel.events)

        viewModel.onDeleteTournament(tournament)
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        runBlocking { Mockito.verify(deleteTournamentLineups)(tournament) }
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

        val events = collectEvents(viewModel.events)

        viewModel.onDeleteTournament(tournament)
        advanceUntilIdle()

        assertEquals(listOf(TournamentDeleteFailed), events)
    }
}
