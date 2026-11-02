/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.ResolveLineupEventTime
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
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
import org.mockito.kotlin.any

/**
 * Unit tests for [LineupCreationViewModel]. Every use case is mocked directly (each already has
 * its own domain-level test suite) - this only verifies the ViewModel's own orchestration and
 * state shaping.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LineupCreationViewModelTest {
    @Mock lateinit var observeTournaments: ObserveTournaments
    @Mock lateinit var getTeam: GetTeam
    @Mock lateinit var getRoster: GetRoster
    @Mock lateinit var createLineup: CreateLineup
    @Mock lateinit var saveTournament: SaveTournament
    @Mock lateinit var toggleRosterPlayerUseCase: ToggleRosterPlayer
    @Mock lateinit var clearRosterUseCase: ClearRoster
    @Mock lateinit var resolveLineupEventTimeUseCase: ResolveLineupEventTime

    private val tournamentsProcessor =
        MutableSharedFlow<List<Tournament>>(replay = 1, extraBufferCapacity = 1)

    lateinit var viewModel: LineupCreationViewModel

    private fun player(id: Long) = RosterPlayerStatus(
        Player(id = id, teamId = 1L, name = "p$id", shirtNumber = id.toInt(), licenseNumber = id),
        status = true,
        playerNumberOverlay = null
    )

    @Before
    fun setup() {
        // init{} eagerly subscribes to observeTournaments() and launches getTeam()/getRoster() -
        // all three need stubbing (even if only leniently) before construction.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Mockito.lenient().`when`(observeTournaments()).thenReturn(tournamentsProcessor)
        runBlocking {
            val team = Team(id = 1L, name = "Panthers", type = TeamType.BASEBALL.id)
            Mockito.lenient().`when`(getTeam()).thenReturn(Result.success(team))
            Mockito.lenient().`when`(getRoster())
                .thenReturn(Result.success(TeamRosterSummary(Constants.STATUS_ALL, emptyList())))
        }

        viewModel = LineupCreationViewModel(
            observeTournaments,
            getTeam,
            getRoster,
            createLineup,
            saveTournament,
            toggleRosterPlayerUseCase,
            clearRosterUseCase,
            resolveLineupEventTimeUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadTeamTypeAndDefaultStrategyOnInit() = runTest {
        advanceUntilIdle()

        assertEquals(TeamType.BASEBALL, viewModel.uiState.value.teamType)
        assertEquals(TeamType.BASEBALL.defaultStrategy, viewModel.uiState.value.strategy)
    }

    @Test
    fun shouldLoadRosterOnInitAndStopLoading() = runTest {
        val roster = TeamRosterSummary(Constants.STATUS_ALL, listOf(player(1L)))
        runBlocking { Mockito.`when`(getRoster()).thenReturn(Result.success(roster)) }

        viewModel = LineupCreationViewModel(
            observeTournaments,
            getTeam,
            getRoster,
            createLineup,
            saveTournament,
            toggleRosterPlayerUseCase,
            clearRosterUseCase,
            resolveLineupEventTimeUseCase
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(roster, viewModel.uiState.value.roster)
    }

    @Test
    fun shouldReflectTournamentsEmittedByObserveTournaments() = runTest {
        val tournament =
            Tournament(id = 1L, name = "Cup", createdAt = 0L, startTime = 0L, endTime = 0L)

        tournamentsProcessor.tryEmit(listOf(tournament))
        advanceUntilIdle()

        assertEquals(listOf(tournament), viewModel.uiState.value.tournaments)
    }

    @Test
    fun shouldDelegateRosterToggleToUseCase() = runTest {
        val toggled = TeamRosterSummary(Constants.STATUS_PARTIAL, listOf(player(1L)))
        Mockito.`when`(toggleRosterPlayerUseCase(viewModel.uiState.value.roster, 0, true))
            .thenReturn(toggled)

        viewModel.onRosterPlayerToggled(0, true)

        assertEquals(toggled, viewModel.uiState.value.roster)
    }

    @Test
    fun shouldDelegateRosterClearToUseCase() = runTest {
        val cleared = TeamRosterSummary(Constants.STATUS_PARTIAL, emptyList())
        Mockito.`when`(clearRosterUseCase(viewModel.uiState.value.roster)).thenReturn(cleared)

        viewModel.onRosterClearAll()

        assertEquals(cleared, viewModel.uiState.value.roster)
    }

    @Test
    fun shouldResolveEventTimeAndSelectTournamentOnTournamentSelected() = runTest {
        val tournament =
            Tournament(id = 1L, name = "Cup", createdAt = 0L, startTime = 10L, endTime = 20L)
        val currentEventTime = viewModel.uiState.value.eventTimeInMillis
        Mockito.`when`(resolveLineupEventTimeUseCase(tournament, currentEventTime)).thenReturn(15L)

        viewModel.onTournamentSelected(tournament)

        val state = viewModel.uiState.value
        assertEquals(tournament, state.selectedTournament)
        assertFalse(state.tournamentError)
        assertFalse(state.showTournamentSheet)
        assertEquals(15L, state.eventTimeInMillis)
    }

    @Test
    fun shouldSaveAndSelectNewTournament() = runTest {
        viewModel.onNewTournamentNameChanged("Cup")
        runBlocking { Mockito.`when`(saveTournament(any())).thenReturn(Result.success(Unit)) }
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Cup", state.selectedTournament?.name)
        assertFalse(state.tournamentError)
        assertFalse(state.showNewTournamentSheet)
        assertEquals(5L, state.eventTimeInMillis)
    }

    @Test
    fun shouldAllowImmediateCancelWhenNothingWasEntered() = runTest {
        val result = viewModel.onCancelRequested()

        assertTrue(result)
        assertFalse(viewModel.uiState.value.showDiscardSheet)
    }

    @Test
    fun shouldRequestDiscardConfirmationWhenNameWasEntered() = runTest {
        viewModel.onNameChanged("My lineup")

        val result = viewModel.onCancelRequested()

        assertFalse(result)
        assertTrue(viewModel.uiState.value.showDiscardSheet)
    }

    @Test
    fun shouldSendLineupSavedEventOnSaveSuccess() = runTest {
        viewModel.onNameChanged("My lineup")
        runBlocking {
            Mockito.`when`(createLineup(any(), any()))
                .thenReturn(Result.success(Lineup(id = 42L)))
        }

        val events = mutableListOf<LineupCreationEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf(LineupSaved(42L)), events)
        job.cancel()
    }

    @Test
    fun shouldFlagNameErrorWhenLineupNameIsEmpty() = runTest {
        runBlocking {
            Mockito.`when`(createLineup(any(), any()))
                .thenReturn(Result.failure(LineupNameEmptyException()))
        }

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.tournamentError)
    }

    @Test
    fun shouldFlagTournamentErrorWhenTournamentIsMissing() = runTest {
        viewModel.onNameChanged("My lineup")
        runBlocking {
            Mockito.`when`(createLineup(any(), any()))
                .thenReturn(Result.failure(TournamentNameEmptyException()))
        }

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tournamentError)
        assertFalse(viewModel.uiState.value.nameError)
    }

    @Test
    fun shouldOpenNewTournamentSheetResettingFields() = runTest {
        viewModel.onNewTournamentNameChanged("stale")

        viewModel.onNewTournamentSheetOpen()

        assertEquals("", viewModel.uiState.value.newTournamentName)
        assertTrue(viewModel.uiState.value.showNewTournamentSheet)
        assertFalse(viewModel.uiState.value.showTournamentSheet)
    }

    @Test
    fun shouldDismissNewTournamentSheet() = runTest {
        viewModel.onNewTournamentSheetOpen()

        viewModel.onNewTournamentSheetDismiss()

        assertFalse(viewModel.uiState.value.showNewTournamentSheet)
    }
}
