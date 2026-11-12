/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.lifecycle.SavedStateHandle
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.Clock
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.ResolveLineupEventTime
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor

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
    private val clock = Clock { NOW }

    private val tournamentsProcessor =
        MutableSharedFlow<List<Tournament>>(replay = 1, extraBufferCapacity = 1)

    lateinit var viewModel: LineupCreationViewModel

    private fun player(id: Long) = RosterPlayerStatus(
        Player(
            id = id,
            teamId = 1L,
            name = "p$id",
            shirtNumber = id.toInt(),
            licenseNumber = id.toString()
        ),
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
            SavedStateHandle(),
            observeTournaments,
            getTeam,
            getRoster,
            createLineup,
            saveTournament,
            toggleRosterPlayerUseCase,
            clearRosterUseCase,
            resolveLineupEventTimeUseCase,
            clock
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
            SavedStateHandle(),
            observeTournaments,
            getTeam,
            getRoster,
            createLineup,
            saveTournament,
            toggleRosterPlayerUseCase,
            clearRosterUseCase,
            resolveLineupEventTimeUseCase,
            clock
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
        val saved = Tournament(id = 7L, name = "Cup", createdAt = 1L, startTime = 0L, endTime = 0L)
        runBlocking { Mockito.`when`(saveTournament(any())).thenReturn(Result.success(saved)) }
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Cup", state.selectedTournament?.name)
        // Regression check: the ViewModel must use the SAVED tournament (real DB id) as
        // selectedTournament, not the pre-insert local one (which would silently default
        // tournamentId to 0 when the lineup is saved afterward).
        assertEquals(7L, state.selectedTournament?.id)
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
        assertEquals(NOW, viewModel.uiState.value.newTournamentStartTime)
        assertEquals(NOW, viewModel.uiState.value.newTournamentEndTime)
        assertTrue(viewModel.uiState.value.showNewTournamentSheet)
        assertFalse(viewModel.uiState.value.showTournamentSheet)
    }

    @Test
    fun shouldStartTheEventTimeAtNow() = runTest {
        assertEquals(NOW, viewModel.uiState.value.eventTimeInMillis)
    }

    @Test
    fun shouldDismissNewTournamentSheet() = runTest {
        viewModel.onNewTournamentSheetOpen()

        viewModel.onNewTournamentSheetDismiss()

        assertFalse(viewModel.uiState.value.showNewTournamentSheet)
    }

    @Test
    fun shouldOpenTournamentSheet() = runTest {
        viewModel.onTournamentSheetOpen()

        assertTrue(viewModel.uiState.value.showTournamentSheet)
    }

    @Test
    fun shouldDismissTournamentSheet() = runTest {
        viewModel.onTournamentSheetOpen()

        viewModel.onTournamentSheetDismiss()

        assertFalse(viewModel.uiState.value.showTournamentSheet)
    }

    @Test
    fun shouldUpdateEventTimeFromTheEventDatePicker() = runTest {
        viewModel.onTournamentSelected(
            Tournament(id = 1L, name = "Cup", createdAt = 0L, startTime = 0L, endTime = 0L)
        )

        viewModel.onEventDateClick()
        assertEquals(LineupDatePicker.EVENT, viewModel.uiState.value.openDatePicker)
        viewModel.onDatePicked(1_700_000_000_000L)

        assertEquals(1_700_000_000_000L, viewModel.uiState.value.eventTimeInMillis)
        assertNull(viewModel.uiState.value.openDatePicker)
    }

    @Test
    fun shouldNotOpenTheEventDatePickerBeforeATournamentIsChosen() = runTest {
        viewModel.onEventDateClick()

        assertNull(viewModel.uiState.value.openDatePicker)
    }

    @Test
    fun shouldKeepTheDatesWhenTheDatePickerIsDismissed() = runTest {
        val before = viewModel.uiState.value
        viewModel.onNewTournamentStartDateClick()

        viewModel.onDatePickerDismiss()

        val after = viewModel.uiState.value
        assertNull(after.openDatePicker)
        assertEquals(before.newTournamentStartTime, after.newTournamentStartTime)
    }

    @Test
    fun shouldOpenRosterSheet() = runTest {
        viewModel.onRosterSheetOpen()

        assertTrue(viewModel.uiState.value.showRosterSheet)
    }

    @Test
    fun shouldDismissRosterSheet() = runTest {
        viewModel.onRosterSheetOpen()

        viewModel.onRosterSheetDismiss()

        assertFalse(viewModel.uiState.value.showRosterSheet)
    }

    @Test
    fun shouldUpdateStrategy() = runTest {
        viewModel.onStrategySelected(TeamStrategy.FIVE_MAN_STANDARD)

        assertEquals(TeamStrategy.FIVE_MAN_STANDARD, viewModel.uiState.value.strategy)
    }

    @Test
    fun shouldUpdateExtraHitters() = runTest {
        viewModel.onExtraHittersSelected(2)

        assertEquals(2, viewModel.uiState.value.extraHitters)
    }

    @Test
    fun shouldUpdateNewTournamentAddress() = runTest {
        viewModel.onNewTournamentAddressChanged("12 Diamond Way")

        assertEquals("12 Diamond Way", viewModel.uiState.value.newTournamentAddress)
    }

    @Test
    fun shouldUpdateNewTournamentStartTime() = runTest {
        viewModel.onNewTournamentStartDateClick()
        viewModel.onDatePicked(1_700_000_000_000L)

        assertEquals(1_700_000_000_000L, viewModel.uiState.value.newTournamentStartTime)
    }

    @Test
    fun shouldUpdateNewTournamentEndTime() = runTest {
        viewModel.onNewTournamentEndDateClick()
        viewModel.onDatePicked(1_700_100_000_000L)

        assertEquals(1_700_100_000_000L, viewModel.uiState.value.newTournamentEndTime)
    }

    @Test
    fun shouldDismissDiscardSheet() = runTest {
        viewModel.onNameChanged("My lineup")
        viewModel.onCancelRequested()

        viewModel.onDiscardDismiss()

        assertFalse(viewModel.uiState.value.showDiscardSheet)
    }

    private fun buildViewModel() = LineupCreationViewModel(
        SavedStateHandle(),
        observeTournaments,
        getTeam,
        getRoster,
        createLineup,
        saveTournament,
        toggleRosterPlayerUseCase,
        clearRosterUseCase,
        resolveLineupEventTimeUseCase,
        clock
    )

    @Test
    fun shouldStopLoadingWhenTheRosterCannotBeRead() = runTest {
        runBlocking {
            Mockito.`when`(getRoster()).thenReturn(Result.failure(Exception("boom")))
        }

        val state = buildViewModel().uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.roster.players.isEmpty())
    }

    @Test
    fun shouldKeepTheDefaultStrategyWhenTheTeamCannotBeRead() = runTest {
        runBlocking {
            Mockito.`when`(getTeam()).thenReturn(Result.failure(Exception("boom")))
        }

        assertEquals(TeamStrategy.STANDARD, buildViewModel().uiState.value.strategy)
    }

    @Test
    fun shouldSaveANewTournamentWithItsAddress() = runTest {
        val captor = argumentCaptor<Tournament>()
        val saved = Tournament(id = 7L, name = "Cup", createdAt = 1L, startTime = 0L, endTime = 0L)
        runBlocking { Mockito.`when`(saveTournament(any())).thenReturn(Result.success(saved)) }
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)
        viewModel.onNewTournamentNameChanged("Cup")
        viewModel.onNewTournamentAddressChanged("Stadium")

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        runBlocking { Mockito.verify(saveTournament)(captor.capture()) }
        assertEquals("Stadium", captor.firstValue.address)
    }

    @Test
    fun shouldSaveABlankAddressAsNoAddress() = runTest {
        val captor = argumentCaptor<Tournament>()
        val saved = Tournament(id = 7L, name = "Cup", createdAt = 1L, startTime = 0L, endTime = 0L)
        runBlocking { Mockito.`when`(saveTournament(any())).thenReturn(Result.success(saved)) }
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)
        viewModel.onNewTournamentAddressChanged("   ")

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        runBlocking { Mockito.verify(saveTournament)(captor.capture()) }
        assertNull(captor.firstValue.address)
    }

    @Test
    fun shouldKeepTheSheetOpenWhenSavingANewTournamentFails() = runTest {
        runBlocking {
            Mockito.`when`(saveTournament(any())).thenReturn(Result.failure(Exception("boom")))
        }
        viewModel.onNewTournamentSheetOpen()
        viewModel.onNewTournamentNameChanged("Cup")
        val events = mutableListOf<LineupCreationEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showNewTournamentSheet)
        assertNull(state.selectedTournament)
        assertNull(state.newTournamentError)
        assertEquals(listOf(NewTournamentSaveFailed), events)
        job.cancel()
    }

    @Test
    fun shouldShowAnEmptyNameErrorInTheNewTournamentSheet() = runTest {
        runBlocking {
            Mockito.`when`(saveTournament(any()))
                .thenReturn(Result.failure(TournamentNameEmptyException()))
        }
        viewModel.onNewTournamentSheetOpen()

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showNewTournamentSheet)
        assertEquals(NewTournamentError.NameEmpty, state.newTournamentError)
    }

    @Test
    fun shouldShowATakenNameErrorInTheNewTournamentSheet() = runTest {
        runBlocking {
            Mockito.`when`(saveTournament(any()))
                .thenReturn(Result.failure(AlreadyExistingTournamentException()))
        }
        viewModel.onNewTournamentSheetOpen()
        viewModel.onNewTournamentNameChanged("Cup")

        viewModel.onAddNewTournament()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showNewTournamentSheet)
        assertEquals(NewTournamentError.NameTaken, state.newTournamentError)
    }

    @Test
    fun shouldClearTheNewTournamentErrorOnceTheNameIsEdited() = runTest {
        runBlocking {
            Mockito.`when`(saveTournament(any()))
                .thenReturn(Result.failure(AlreadyExistingTournamentException()))
        }
        viewModel.onNewTournamentSheetOpen()
        viewModel.onNewTournamentNameChanged("Cup")
        viewModel.onAddNewTournament()
        advanceUntilIdle()

        viewModel.onNewTournamentNameChanged("Cup 2")

        assertNull(viewModel.uiState.value.newTournamentError)
    }

    @Test
    fun shouldNotCarryANewTournamentErrorIntoTheNextSheet() = runTest {
        runBlocking {
            Mockito.`when`(saveTournament(any()))
                .thenReturn(Result.failure(TournamentNameEmptyException()))
        }
        viewModel.onNewTournamentSheetOpen()
        viewModel.onAddNewTournament()
        advanceUntilIdle()
        viewModel.onNewTournamentSheetDismiss()

        viewModel.onNewTournamentSheetOpen()

        assertNull(viewModel.uiState.value.newTournamentError)
    }

    @Test
    fun shouldRequestDiscardConfirmationWhenATournamentWasPicked() = runTest {
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)
        viewModel.onTournamentSelected(
            Tournament(id = 7L, name = "Cup", createdAt = 1L, startTime = 0L, endTime = 0L)
        )

        assertFalse(viewModel.onCancelRequested())
        assertTrue(viewModel.uiState.value.showDiscardSheet)
    }

    @Test
    fun shouldSaveTheLineupUnderTheSelectedTournament() = runTest {
        val captor = argumentCaptor<Lineup>()
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)
        runBlocking {
            Mockito.`when`(createLineup(any(), any())).thenReturn(Result.success(Lineup(id = 3L)))
        }
        viewModel.onTournamentSelected(
            Tournament(id = 7L, name = "Cup", createdAt = 1L, startTime = 0L, endTime = 0L)
        )

        viewModel.onSaveClick()
        advanceUntilIdle()

        runBlocking { Mockito.verify(createLineup)(captor.capture(), any()) }
        assertEquals(7L, captor.firstValue.tournamentId)
    }

    @Test
    fun shouldFlagNoFieldWhenSavingFailsForAnotherReason() = runTest {
        runBlocking {
            Mockito.`when`(createLineup(any(), any()))
                .thenReturn(Result.failure(IllegalStateException("db error")))
        }

        val events = mutableListOf<LineupCreationEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.tournamentError)
        assertEquals(listOf(LineupSaveFailed), events)
        job.cancel()
    }

    private fun viewModelWith(handle: SavedStateHandle) = LineupCreationViewModel(
        handle,
        observeTournaments,
        getTeam,
        getRoster,
        createLineup,
        saveTournament,
        ToggleRosterPlayer(),
        clearRosterUseCase,
        resolveLineupEventTimeUseCase,
        clock
    )

    @Test
    fun shouldRestoreEveryChoiceAfterTheProcessWasKilled() = runTest {
        val handle = SavedStateHandle()
        val tournament =
            Tournament(id = 7L, name = "Cup", createdAt = 1L, startTime = 0L, endTime = 0L)
        val roster = TeamRosterSummary(Constants.STATUS_ALL, listOf(player(1L), player(2L)))
        runBlocking { Mockito.`when`(getRoster()).thenReturn(Result.success(roster)) }
        Mockito.`when`(resolveLineupEventTimeUseCase(any(), any())).thenReturn(5L)
        viewModelWith(handle).apply {
            onNameChanged("Scrimmage")
            onTournamentSelected(tournament)
            onStrategySelected(TeamStrategy.SLOWPITCH)
            onExtraHittersSelected(2)
            onRosterPlayerToggled(1, false)
        }

        val state = viewModelWith(handle).uiState.value

        assertEquals("Scrimmage", state.name)
        assertEquals(7L, state.selectedTournament?.id)
        assertEquals(5L, state.eventTimeInMillis)
        assertEquals(TeamStrategy.SLOWPITCH, state.strategy)
        assertEquals(2, state.extraHitters)
        assertEquals(listOf(true, false), state.roster.players.map { it.status })
        assertEquals(Constants.STATUS_PARTIAL, state.roster.status)
    }

    @Test
    fun shouldUseTheTeamsDefaultStrategyWithoutASavedForm() = runTest {
        runBlocking {
            val team = Team(id = 1L, name = "B5", type = TeamType.BASEBALL_5.id)
            Mockito.`when`(getTeam()).thenReturn(Result.success(team))
        }

        assertEquals(
            TeamStrategy.B5_DEFAULT,
            viewModelWith(SavedStateHandle()).uiState.value.strategy
        )
    }

    private companion object {
        const val NOW = 1_760_000_000_000L
    }
}
