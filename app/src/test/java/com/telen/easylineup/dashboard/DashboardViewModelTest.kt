/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TilesRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveTeams
import com.telen.easylineup.testDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
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
 * Characterization tests for [DashboardViewModel]: real UseCases wired to mocked repositories,
 * constructor-injected directly (no Koin needed now that the ViewModel takes its dependencies
 * as constructor params instead of resolving them as a [org.koin.core.component.KoinComponent]).
 */
@RunWith(MockitoJUnitRunner::class)
internal class DashboardViewModelTest {

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var playerRepository: PlayerRepository

    @Mock
    lateinit var playerNumberOverlayRepository: PlayerNumberOverlayRepository

    @Mock
    lateinit var lineupRepository: LineupRepository

    @Mock
    lateinit var playerFieldPositionRepository: PlayerFieldPositionRepository

    @Mock
    lateinit var tilesRepository: TilesRepository

    @Mock
    lateinit var tournamentRepository: TournamentRepository

    lateinit var viewModel: DashboardViewModel
    private val teamsProcessor = MutableSharedFlow<List<Team>>(replay = 1, extraBufferCapacity = 1)

    @Before
    fun setup() {
        // DashboardViewModel now collects its tiles flow eagerly in init{} via
        // viewModelScope.launch, which needs Dispatchers.Main set up - and observeTeams()
        // stubbed - before construction.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Mockito.lenient().`when`(teamRepository.observeTeams()).thenReturn(teamsProcessor)

        runBlocking {
            // lenient: each test only exercises one of these sub-ports
            Mockito.lenient().`when`(teamRepository.getTeams())
                .thenReturn(listOf(Team(id = 1L, name = "Panthers", main = true)))
        }

        val dispatcherProvider = testDispatcherProvider()
        val getTeam = GetTeam(teamRepository, dispatcherProvider)

        viewModel = DashboardViewModel(
            observeTeams = ObserveTeams(teamRepository),
            getDashboardTilesUseCase = GetDashboardTiles(
                playerRepository,
                lineupRepository,
                playerFieldPositionRepository,
                tilesRepository,
                tournamentRepository,
                getTeam,
                CreateDashboardTiles(tilesRepository, dispatcherProvider),
                dispatcherProvider
            ),
            getShirtNumberHistoryUseCase = GetShirtNumberHistory(
                playerRepository,
                playerNumberOverlayRepository,
                getTeam,
                dispatcherProvider
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldUpdateUiStateWhenTeamsChange() = runTest {
        val tile = DashboardTile(id = 1L, position = 0, type = TileType.TEAM_SIZE.type)
        Mockito.`when`(tilesRepository.getTiles()).thenReturn(listOf(tile))
        Mockito.`when`(playerRepository.getPlayersByTeamId(1L)).thenReturn(emptyList())

        assertTrue(viewModel.uiState.value.isLoading)

        teamsProcessor.tryEmit(emptyList())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun shouldSearchShirtNumberDelegateToUseCase() = runTest {
        val overlayEntry = ShirtNumberEntry(
            number = 8,
            playerName = "Toto",
            playerId = 1L,
            eventTime = 0L,
            createdAt = 0L,
            lineupId = 1L,
            lineupName = "Game 1"
        )
        Mockito.`when`(playerRepository.getShirtNumberFromPlayers(1L, 8))
            .thenReturn(emptyList())
        Mockito.`when`(playerNumberOverlayRepository.getShirtNumberFromNumberOverlays(1L, 8))
            .thenReturn(listOf(overlayEntry))

        viewModel.onShirtNumberQueryChanged("8")
        viewModel.onShirtNumberSearch()
        advanceUntilIdle()

        assertEquals(listOf(overlayEntry), viewModel.uiState.value.shirtNumberHistory)
        assertEquals("Toto", viewModel.uiState.value.shirtNumberResult)
    }
}
