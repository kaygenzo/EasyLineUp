/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSortType
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayers
import com.telen.easylineup.domain.usecases.SortPlayers
import com.telen.easylineup.domain.usecases.exceptions.NoTeamsLeftException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
import org.mockito.kotlin.any

/**
 * Unit tests for [TeamViewModel]: its use cases are mocked directly (each already has its own
 * domain-level test suite). `init {}` eagerly loads the team and flatMaps into the observed
 * players Flow, so both [getTeamUseCase]'s and [observePlayersUseCase]'s stubs must be in place
 * before construction - same gotcha as [com.telen.easylineup.dashboard.DashboardViewModelTest].
 */
@RunWith(MockitoJUnitRunner::class)
internal class TeamViewModelTest {
    @Mock
    lateinit var getTeamUseCase: GetTeam

    @Mock
    lateinit var deleteTeamUseCase: DeleteTeam

    @Mock
    lateinit var observePlayersUseCase: ObservePlayers

    @Mock
    lateinit var sortPlayersUseCase: SortPlayers

    private val playersProcessor =
        MutableSharedFlow<List<Player>>(replay = 1, extraBufferCapacity = 1)
    private val team = Team(id = 1L, name = "Panthers", type = 0, main = true, createdAt = 100L)

    lateinit var viewModel: TeamViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        runBlocking {
            Mockito.lenient().`when`(getTeamUseCase()).thenReturn(Result.success(team))
        }
        Mockito.lenient().`when`(observePlayersUseCase(team.id)).thenReturn(playersProcessor)
        Mockito.lenient().`when`(sortPlayersUseCase(any(), any()))
            .thenAnswer { invocation -> invocation.arguments[0] }

        viewModel = TeamViewModel(
            getTeamUseCase,
            deleteTeamUseCase,
            observePlayersUseCase,
            sortPlayersUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadTeamOnInit() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Panthers", state.teamName)
        assertEquals(TeamType.getTypeById(0), state.teamType)
        assertEquals(100L, state.teamCreatedAt)
    }

    @Test
    fun shouldUpdatePlayersWhenObservedFlowEmits() = runTest {
        val players = listOf(
            Player(id = 1L, teamId = 1L, name = "Alice", shirtNumber = 1, licenseNumber = "1"),
            Player(id = 2L, teamId = 1L, name = "Bob", shirtNumber = 2, licenseNumber = "2")
        )

        playersProcessor.tryEmit(players)
        advanceUntilIdle()

        assertEquals(players, viewModel.uiState.value.players)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun newViewModel() = TeamViewModel(
        getTeamUseCase,
        deleteTeamUseCase,
        observePlayersUseCase,
        sortPlayersUseCase
    )

    @Test
    fun shouldReportALoadFailureWhenThePlayersCannotBeRead() = runTest {
        Mockito.`when`(observePlayersUseCase(team.id))
            .thenReturn(flow { throw IllegalStateException("db error") })

        viewModel = newViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadFailed)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun shouldReportALoadFailureWhenTheTeamCannotBeRead() = runTest {
        runBlocking {
            Mockito.`when`(getTeamUseCase()).thenReturn(Result.failure(Exception("db error")))
        }

        viewModel = newViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadFailed)
    }

    @Test
    fun shouldReadTheTeamAndItsPlayersAgainOnRetry() = runTest {
        val players = listOf(
            Player(id = 1L, teamId = 1L, name = "Alice", shirtNumber = 1, licenseNumber = "1")
        )
        Mockito.`when`(observePlayersUseCase(team.id))
            .thenReturn(flow { throw IllegalStateException("db error") })
            .thenReturn(flowOf(players))
        viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.onRetryLoad()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loadFailed)
        assertEquals(players, viewModel.uiState.value.players)
    }

    @Test
    fun shouldApplySortTypeWhenChanged() = runTest {
        val alice =
            Player(id = 1L, teamId = 1L, name = "Alice", shirtNumber = 2, licenseNumber = "1")
        val bob = Player(id = 2L, teamId = 1L, name = "Bob", shirtNumber = 1, licenseNumber = "2")
        playersProcessor.tryEmit(listOf(alice, bob))
        advanceUntilIdle()
        Mockito.`when`(sortPlayersUseCase(listOf(alice, bob), PlayerSortType.NUMERIC))
            .thenReturn(listOf(bob, alice))

        viewModel.setSortType(PlayerSortType.NUMERIC)
        advanceUntilIdle()

        assertEquals(PlayerSortType.NUMERIC, viewModel.uiState.value.sortType)
        assertEquals(listOf(bob, alice), viewModel.uiState.value.players)
    }

    /** Starts collecting [flow] on the test scheduler, for the rest of the test. */
    private fun <T> TestScope.collectEvents(flow: Flow<T>): List<T> {
        val events = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(events) }
        return events
    }

    @Test
    fun shouldDeleteTeamSuccessfully() = runTest {
        runBlocking { Mockito.`when`(deleteTeamUseCase(team)).thenReturn(Result.success(Unit)) }
        val events = collectEvents(viewModel.events)

        viewModel.onDeleteTeam(team)

        assertEquals(listOf(TeamEvent.TeamDeleted), events)
    }

    @Test
    fun shouldTreatDeletingTheLastTeamAsADelete() = runTest {
        runBlocking {
            Mockito.`when`(deleteTeamUseCase(team))
                .thenReturn(Result.failure(NoTeamsLeftException()))
        }
        val events = collectEvents(viewModel.events)

        viewModel.onDeleteTeam(team)

        assertEquals(listOf(TeamEvent.TeamDeleted), events)
    }

    @Test
    fun shouldPropagateDeleteTeamFailure() = runTest {
        val exception = Exception("db error")
        runBlocking {
            Mockito.`when`(deleteTeamUseCase(team)).thenReturn(Result.failure(exception))
        }

        val events = collectEvents(viewModel.events)

        viewModel.onDeleteTeam(team)

        assertEquals(listOf(TeamEvent.TeamDeleteFailed), events)
    }
}
