/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.HasUnsavedLineupEdits
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import com.telen.easylineup.domain.usecases.SaveLineupChanges
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [LineupEditionViewModel]: the Tournaments list's Edit Lineup sheet, from
 * loading a lineup into the form to saving or discarding it. The use cases that read or write
 * the database are mocked; the pure roster ones are real, so the form's roster really changes.
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
    private val ana = RosterPlayerStatus(Player(1L, 1L, "Ana", 4, "L1"), true, null)
    private val roster = TeamRosterSummary(Constants.STATUS_ALL, listOf(ana))

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
            observeTournaments,
            ToggleRosterPlayer(),
            ClearRoster(),
            OverrideRosterPlayerNumber(),
            HasUnsavedLineupEdits()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun openForm() {
        runBlocking {
            Mockito.`when`(getLineupById(1L)).thenReturn(Result.success(lineup))
            Mockito.`when`(getRoster(1L)).thenReturn(Result.success(roster))
        }
        viewModel.onEditLineup(1L)
    }

    private val form get() = viewModel.uiState.value.form

    @Test
    fun shouldExposeTournamentsFromObserveTournaments() = runTest {
        advanceUntilIdle()

        assertEquals(listOf(tournament), viewModel.uiState.value.tournaments)
    }

    @Test
    fun theSheetIsClosedUntilALineupIsLoaded() = runTest {
        assertNull(form)
    }

    @Test
    fun loadingALineupOpensTheFormOnIt() = runTest {
        openForm()

        val form = form!!
        assertEquals("Game 1", form.name)
        assertEquals(tournament, form.tournament)
        assertEquals(roster, form.roster)
        assertEquals(EditLineupStep.FIELDS, form.step)
        assertFalse(form.nameError)
    }

    @Test
    fun aLineupThatFailsToLoadLeavesTheSheetClosed() = runTest {
        runBlocking {
            Mockito.`when`(getLineupById(1L)).thenReturn(Result.failure(Exception("not found")))
        }

        val events = mutableListOf<LineupEditionEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onEditLineup(1L)
        advanceUntilIdle()

        assertNull(form)
        assertEquals(listOf(LineupLoadFailed), events)
        job.cancel()
    }

    @Test
    fun cancellingAnUntouchedFormClosesIt() = runTest {
        openForm()

        viewModel.onCancel()

        assertNull(form)
    }

    @Test
    fun cancellingAfterRenamingAsksFirst() = runTest {
        openForm()
        viewModel.onNameChanged("Game 2")

        viewModel.onCancel()

        assertTrue(form!!.showDiscardSheet)
        viewModel.onDiscardDismiss()
        assertFalse(form!!.showDiscardSheet)
        assertEquals("Game 2", form!!.name)
    }

    @Test
    fun cancellingAfterARosterChangeAsksFirst() = runTest {
        openForm()
        viewModel.onPlayerToggled(0, false)

        viewModel.onCancel()

        assertTrue(form!!.showDiscardSheet)
    }

    @Test
    fun confirmingTheDiscardClosesTheForm() = runTest {
        openForm()
        viewModel.onNameChanged("Game 2")
        viewModel.onCancel()

        viewModel.onDiscardConfirm()

        assertNull(form)
    }

    @Test
    fun theRosterStepAndTheTournamentPickerOpenAndClose() = runTest {
        openForm()

        viewModel.onRosterClick()
        assertEquals(EditLineupStep.ROSTER, form!!.step)
        viewModel.onRosterBackClick()
        assertEquals(EditLineupStep.FIELDS, form!!.step)
        viewModel.onTournamentClick()
        assertTrue(form!!.showTournamentSheet)
        viewModel.onTournamentSheetDismiss()
        assertFalse(form!!.showTournamentSheet)
    }

    @Test
    fun rosterEditsApplyToTheForm() = runTest {
        openForm()

        viewModel.onNumberChanged(0, 42)
        assertEquals(42, form!!.roster.players[0].playerNumberOverlay?.number)
        assertEquals(1L, form!!.roster.players[0].playerNumberOverlay?.lineupId)
        viewModel.onClearAll()
        assertFalse(form!!.roster.players[0].status)
    }

    @Test
    fun savingAppliesTheEditsToTheLoadedLineupAndClosesTheForm() = runTest {
        val other = Tournament(id = 2L, name = "Fall", createdAt = 0L, startTime = 0L, endTime = 0L)
        val expected = lineup.copy(name = "Game 2", tournamentId = 2L)
        runBlocking {
            Mockito.`when`(saveLineupChanges(expected, roster.players))
                .thenReturn(Result.success(Unit))
        }
        openForm()
        viewModel.onNameChanged("Game 2")
        viewModel.onTournamentSelected(other)

        viewModel.onSave()
        advanceUntilIdle()

        assertNull(form)
    }

    @Test
    fun aBlankNameIsFlaggedOnTheForm() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup.copy(name = " "), roster.players))
                .thenReturn(Result.failure(LineupNameEmptyException()))
        }
        openForm()
        viewModel.onNameChanged(" ")

        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(form!!.nameError)
        viewModel.onNameChanged("Game 3")
        assertFalse(form!!.nameError)
    }

    @Test
    fun aMissingTournamentIsFlaggedOnTheForm() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup, roster.players))
                .thenReturn(Result.failure(TournamentNameEmptyException()))
        }
        openForm()

        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(form!!.tournamentError)
        viewModel.onTournamentSelected(tournament)
        assertFalse(form!!.tournamentError)
    }

    @Test
    fun shouldReportAnUnrelatedFailureAndKeepTheForm() = runTest {
        runBlocking {
            Mockito.`when`(saveLineupChanges(lineup, roster.players))
                .thenReturn(Result.failure(Exception("db error")))
        }
        openForm()

        val events = mutableListOf<LineupEditionEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(listOf(LineupUpdateFailed), events)
        assertEquals("Game 1", form?.name)
        job.cancel()
    }
}
