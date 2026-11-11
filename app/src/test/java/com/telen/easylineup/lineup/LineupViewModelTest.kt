/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.LineupDraft
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.usecases.ApplyRosterToLineup
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveLineupById
import com.telen.easylineup.domain.usecases.ObservePlayerNumberOverlays
import com.telen.easylineup.domain.usecases.ObserveTeamPlayersAndMaybePositionsForLineup
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SaveLineupDraft
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.testDispatcherProvider
import com.telen.easylineup.utils.LineupExportData
import com.telen.easylineup.utils.LineupPdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq

/**
 * Unit tests for [LineupViewModel]. The three pure roster use cases (already covered by their
 * own domain tests) are used as real instances rather than mocked, since stubbing every possible
 * input/output pair would just re-implement them; everything else (I/O, Flow-returning use
 * cases) is mocked directly.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LineupViewModelTest {
    @Mock lateinit var getTeamUseCase: GetTeam
    @Mock lateinit var observePlayerNumberOverlays: ObservePlayerNumberOverlays
    @Mock lateinit var deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition
    @Mock lateinit var switchPlayersPositionUseCase: SwitchPlayersPosition
    @Mock lateinit var assignPlayerFieldPositionUseCase: AssignPlayerFieldPosition
    @Mock
    lateinit var getListAvailablePlayersForSelectionUseCase: GetListAvailablePlayersForSelection
    @Mock lateinit var saveLineupDraftUseCase: SaveLineupDraft
    @Mock lateinit var getRosterUseCase: GetRoster
    @Mock lateinit var deleteLineupUseCase: DeleteLineup
    @Mock lateinit var setLineupModeUseCase: SetLineupMode
    @Mock lateinit var getDpAndFlexFromPlayersInFieldUseCase: GetDpAndFlexFromPlayersInField
    @Mock lateinit var getBattersStateUseCase: GetBattersState
    @Mock lateinit var observeTeamPlayersUseCase: ObserveTeamPlayersAndMaybePositionsForLineup
    @Mock lateinit var observeLineupByIdUseCase: ObserveLineupById
    @Mock lateinit var getOnlyPlayersInFieldUseCase: GetOnlyPlayersInField
    @Mock lateinit var saveDpAndFlexUseCase: SaveDpAndFlex
    @Mock lateinit var updatePlayersWithBattersUseCase: UpdatePlayersWithBatters
    @Mock lateinit var lineupPdfWriter: LineupPdfWriter

    /** The navigation arguments the ViewModel is opened with, set before its first use. */
    private var routeLineupId = 1L
    private var editable = false

    /** Built on first use, so a test can stub the observations and pick [editable] first. */
    private val viewModel: LineupViewModel by lazy { buildViewModel() }

    private val lineupFlow = MutableSharedFlow<Lineup>(replay = 1, extraBufferCapacity = 1)
    private val playersFlow =
        MutableSharedFlow<List<PlayerWithPosition>>(replay = 1, extraBufferCapacity = 1)
    private val overlaysFlow =
        MutableSharedFlow<List<PlayerNumberOverlay>>(replay = 1, extraBufferCapacity = 1)

    private fun player(id: Long, position: Int = -1, order: Int = 0) = PlayerWithPosition(
        playerName = "p$id",
        playerSex = 0,
        shirtNumber = id.toInt(),
        licenseNumber = id.toString(),
        teamId = 1L,
        image = null,
        position = position,
        order = order,
        playerId = id,
        lineupId = 1L,
        playerPositions = 0
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        Mockito.lenient().`when`(observeLineupByIdUseCase(any())).thenReturn(lineupFlow)
        Mockito.lenient().`when`(observeTeamPlayersUseCase(any())).thenReturn(playersFlow)
        Mockito.lenient().`when`(observePlayerNumberOverlays(any())).thenReturn(overlaysFlow)
        runBlocking {
            Mockito.lenient().`when`(getTeamUseCase()).thenReturn(
                Result.success(
                    Team(id = 1L, name = "Team", type = TeamType.BASEBALL.id, main = true)
                )
            )
            Mockito.lenient().`when`(
                getBattersStateUseCase(any(), any(), any(), any(), any(), any())
            ).thenReturn(Result.success(emptyList()))
        }
    }

    private fun buildViewModel(): LineupViewModel = LineupViewModel(
        SavedStateHandle(mapOf("lineupId" to routeLineupId, "editable" to editable)),
        getTeamUseCase,
        observePlayerNumberOverlays,
        deletePlayerFieldPositionUseCase,
        switchPlayersPositionUseCase,
        assignPlayerFieldPositionUseCase,
        getListAvailablePlayersForSelectionUseCase,
        saveLineupDraftUseCase,
        deleteLineupUseCase,
        setLineupModeUseCase,
        getDpAndFlexFromPlayersInFieldUseCase,
        getBattersStateUseCase,
        observeTeamPlayersUseCase,
        observeLineupByIdUseCase,
        getOnlyPlayersInFieldUseCase,
        saveDpAndFlexUseCase,
        updatePlayersWithBattersUseCase,
        LineupRosterUseCases(
            getRosterUseCase,
            ApplyRosterToLineup(DeletePlayerFieldPosition(testDispatcherProvider())),
            ToggleRosterPlayer(),
            ClearRoster(),
            OverrideRosterPlayerNumber()
        ),
        lineupPdfWriter,
        testDispatcherProvider()
    )

    /**
     * A stub for an editing use case that changes nothing: it returns the players it got. The
     * answer is the bare list: a suspend function hands its `Result` back unboxed, so a boxed
     * one returned from an [Answer] would reach the caller as the wrong type.
     */
    private fun returnsPlayersArgument(index: Int) = Answer {
        it.getArgument<List<PlayerWithPosition>>(index)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loadLineup(
        lineup: Lineup,
        players: List<PlayerWithPosition> = emptyList(),
        overlays: List<PlayerNumberOverlay> = emptyList()
    ) {
        routeLineupId = lineup.id
        viewModel
        lineupFlow.emit(lineup)
        playersFlow.emit(players)
        overlaysFlow.emit(overlays)
    }

    @Test
    fun shouldObserveTheLineupNamedByItsNavigationArgumentsAsSoonAsItIsCreated() {
        routeLineupId = 7L
        editable = true

        assertEquals(7L, viewModel.lineupId)
        assertTrue(viewModel.editable)
        Mockito.verify(observeLineupByIdUseCase).invoke(7L)
    }

    @Test
    fun shouldReportALoadFailureWhenTheLineupCannotBeRead() = runTest {
        Mockito.`when`(observeLineupByIdUseCase(1L))
            .thenReturn(flow { throw IllegalStateException("db error") })

        viewModel
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadFailed)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun shouldReadTheLineupAgainOnRetry() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        Mockito.`when`(observeLineupByIdUseCase(1L))
            .thenReturn(flow { throw IllegalStateException("db error") })
            .thenReturn(lineupFlow)
        viewModel
        advanceUntilIdle()

        viewModel.onRetryLoad()
        loadLineup(lineup)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loadFailed)
        assertEquals("L", viewModel.uiState.value.lineupName)
    }

    @Test
    fun shouldKeepTheLoadedLineupOnScreenWhenALaterReadFails() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val failingOverlays = MutableSharedFlow<List<PlayerNumberOverlay>>(replay = 1)
        Mockito.`when`(observePlayerNumberOverlays(1L)).thenReturn(
            flow {
                emit(emptyList())
                failingOverlays.collect { throw IllegalStateException("db error") }
            }
        )
        loadLineup(lineup)
        advanceUntilIdle()

        failingOverlays.emit(emptyList())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loadFailed)
        assertEquals("L", viewModel.uiState.value.lineupName)
    }

    @Test
    fun shouldPopulateStateWhenLineupLoads() = runTest {
        val lineup = Lineup(
            id = 1L,
            name = "Test Lineup",
            teamId = 1L,
            tournamentId = 1L,
            strategy = TeamStrategy.STANDARD.id,
            extraHitters = 1
        )
        loadLineup(lineup, listOf(player(1L)))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Test Lineup", state.lineupName)
        assertEquals(TeamStrategy.STANDARD, state.defenseStrategy)
        assertEquals(1, state.extraHitters)
        assertEquals(TeamType.BASEBALL.id, state.teamType)
        assertFalse(state.isLoading)
        assertEquals(1, state.defensePlayers.size)
    }

    @Test
    fun shouldApplyShirtNumberOverlaysOntoDefensePlayers() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val overlay = PlayerNumberOverlay(lineupId = 1L, playerId = 1L, number = 42)
        loadLineup(lineup, listOf(player(1L)), listOf(overlay))
        advanceUntilIdle()

        assertEquals(42, viewModel.uiState.value.defensePlayers.first().shirtNumber)
    }

    @Test
    fun shouldDeletePositionOnSuccess() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup, listOf(player(1L)))
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(
                deletePlayerFieldPositionUseCase(any(), any(), any(), any())
            ).thenAnswer(returnsPlayersArgument(0))
        }

        viewModel.onDeletePosition(
            Player(id = 1L, teamId = 1L, name = "p1", shirtNumber = 1, licenseNumber = "1")
        )
        advanceUntilIdle()

        runBlocking {
            Mockito.verify(deletePlayerFieldPositionUseCase)(any(), any(), any(), any())
        }
    }

    /** Starts collecting [flow] on the test scheduler, for the rest of the test. */
    private fun <T> TestScope.collectEvents(flow: Flow<T>): List<T> {
        val events = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(events) }
        return events
    }

    @Test
    fun shouldNotSaveWhenLineupIsNull() = runTest {
        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        Mockito.verifyNoInteractions(saveLineupDraftUseCase)
    }

    @Test
    fun shouldSaveSuccessfullyWhenLineupIsLoaded() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(saveLineupDraftUseCase(eq(lineup), any(), any()))
                .thenReturn(Result.success(Unit))
        }

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.Saved), events)
    }

    @Test
    fun shouldReportAFailedSaveAndKeepTheDraftUnsaved() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(saveLineupDraftUseCase(eq(lineup), any(), any()))
                .thenReturn(Result.failure(IllegalStateException("db error")))
        }
        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.SaveFailed), events)
    }

    @Test
    fun shouldDeleteLineupThroughUseCase() = runTest {
        viewModel
        runBlocking {
            Mockito.`when`(deleteLineupUseCase(1L)).thenReturn(Result.success(Unit))
        }

        val events = collectEvents(viewModel.events)

        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.Deleted), events)
    }

    @Test
    fun shouldReportAFailedDelete() = runTest {
        viewModel
        runBlocking {
            Mockito.`when`(deleteLineupUseCase(1L))
                .thenReturn(Result.failure(IllegalStateException("db error")))
        }
        val events = collectEvents(viewModel.events)

        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.DeleteFailed), events)
    }

    @Test
    fun shouldUpdateLineupModeOnSuccess() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val updatedLineup = lineup.copy(mode = 1)
        runBlocking {
            Mockito.`when`(setLineupModeUseCase(eq(true), eq(lineup), any())).thenAnswer {
                LineupDraft(updatedLineup, it.getArgument(2))
            }
        }

        viewModel.onLineupModeChanged(true)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.lineupMode)
    }

    @Test
    fun shouldHandTheWrittenPdfOverForSharing() = runTest {
        val lineup = Lineup(id = 1L, name = "My Lineup", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val uri = Mockito.mock(Uri::class.java)
        Mockito.`when`(lineupPdfWriter.write(any())).thenReturn(uri)
        val events = collectEvents(viewModel.events)

        viewModel.onShareClick()
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.ShareReady(uri, "My Lineup")), events)
    }

    @Test
    fun shouldReportAShareFailureWhenThePdfCannotBeWritten() = runTest {
        Mockito.`when`(lineupPdfWriter.write(any())).thenThrow(IllegalStateException("disk full"))
        val events = collectEvents(viewModel.events)

        viewModel.onShareClick()
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.ShareFailed), events)
    }

    @Test
    fun shouldAssignPlayerAndClearAvailablePlayersEvent() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup, listOf(player(2L)))
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(assignPlayerFieldPositionUseCase(any(), any(), eq(lineup), any()))
                .thenAnswer(returnsPlayersArgument(3))
        }

        viewModel.onPlayerSelected(
            Player(id = 2L, teamId = 1L, name = "p2", shirtNumber = 2, licenseNumber = "2"),
            FieldPosition.PITCHER
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.availablePlayersEvent)
    }

    @Test
    fun shouldShowAvailablePlayersSheetWhenPositionClickedWithCandidates() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val candidates = listOf(player(3L))
        runBlocking {
            Mockito.`when`(
                getListAvailablePlayersForSelectionUseCase(
                    any(), eq(FieldPosition.PITCHER), eq(lineup)
                )
            )
                .thenReturn(Result.success(candidates))
        }

        val result = viewModel.onPositionClicked(FieldPosition.PITCHER)

        assertTrue(result.isSuccess)
        assertEquals(candidates, viewModel.uiState.value.availablePlayersEvent?.players)
    }

    @Test
    fun shouldShowInfoMessageWhenNoCandidatesForPosition() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(
                getListAvailablePlayersForSelectionUseCase(
                    any(), eq(FieldPosition.PITCHER), eq(lineup)
                )
            )
                .thenReturn(Result.success(emptyList()))
        }

        viewModel.onPositionClicked(FieldPosition.PITCHER)

        assertNull(viewModel.uiState.value.availablePlayersEvent)
        assertEquals(
            LineupInfoMessage.NoCandidatesForPosition,
            viewModel.uiState.value.infoMessage
        )
    }

    @Test
    fun shouldShowDpFlexSheetOnDpDhClicked() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val config = DpAndFlexConfiguration(
            dp = null,
            flex = null,
            dpLocked = false,
            flexLocked = false,
            teamType = TeamType.BASEBALL.id
        )
        runBlocking {
            Mockito.`when`(getDpAndFlexFromPlayersInFieldUseCase(any()))
                .thenReturn(Result.success(config))
        }

        val result = viewModel.onPositionClicked(FieldPosition.DP_DH)

        assertTrue(result.isSuccess)
        assertEquals(TeamType.BASEBALL.id, viewModel.uiState.value.dpFlexEvent?.teamType)
    }

    @Test
    fun shouldShowInfoMessageWhenPitcherMissing() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(getDpAndFlexFromPlayersInFieldUseCase(any()))
                .thenReturn(Result.failure(NeedAssignPitcherFirstException()))
        }

        val result = viewModel.onPositionClicked(FieldPosition.DP_DH)

        assertTrue(result.isFailure)
        assertEquals(
            LineupInfoMessage.PitcherAssignmentRequired,
            viewModel.uiState.value.infoMessage
        )
    }

    @Test
    fun shouldSwitchPlayersPositionOnSuccess() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup, listOf(player(1L, position = FieldPosition.PITCHER.id)))
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(
                switchPlayersPositionUseCase(any(), any(), any(), eq(lineup))
            ).thenAnswer(returnsPlayersArgument(0))
        }

        viewModel.switchPlayersPosition(
            player(1L, position = FieldPosition.PITCHER.id),
            FieldPosition.CATCHER
        )
        advanceUntilIdle()

        runBlocking {
            Mockito.verify(switchPlayersPositionUseCase)(any(), any(), any(), eq(lineup))
        }
    }

    @Test
    fun shouldDismissAvailablePlayersEvent() {
        viewModel.onAvailablePlayersDismiss()
        assertNull(viewModel.uiState.value.availablePlayersEvent)
    }

    @Test
    fun shouldDismissDpFlexEvent() {
        viewModel.onDpFlexDismiss()
        assertNull(viewModel.uiState.value.dpFlexEvent)
    }

    @Test
    fun shouldSetAndClearDeleteConfirmPlayer() {
        val target = Player(id = 1L, teamId = 1L, name = "p1", shirtNumber = 1, licenseNumber = "1")
        viewModel.onSubstituteLongClicked(target)
        assertEquals(target, viewModel.uiState.value.deleteConfirmPlayer)

        viewModel.onDeleteConfirmDismiss()
        assertNull(viewModel.uiState.value.deleteConfirmPlayer)
    }

    @Test
    fun shouldDismissInfoMessage() {
        viewModel.onInfoMessageDismiss()
        assertNull(viewModel.uiState.value.infoMessage)
    }

    @Test
    fun shouldToggleDiscardSheet() {
        viewModel.onDiscardSheetRequested()
        assertTrue(viewModel.uiState.value.showDiscardSheet)

        viewModel.onDiscardSheetDismiss()
        assertFalse(viewModel.uiState.value.showDiscardSheet)
    }

    @Test
    fun shouldToggleDeleteSheet() {
        viewModel.onDeleteSheetRequested()
        assertTrue(viewModel.uiState.value.showDeleteSheet)

        viewModel.onDeleteSheetDismiss()
        assertFalse(viewModel.uiState.value.showDeleteSheet)
    }

    @Test
    fun shouldGetPlayerSelectionForDpRecoveringFromFailure() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(getListAvailablePlayersForSelectionUseCase(any(), eq(null), eq(lineup)))
                .thenReturn(Result.failure(Exception("boom")))
        }

        val result = viewModel.getPlayerSelectionForDp()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<PlayerWithPosition>(), result.getOrNull())
    }

    @Test
    fun shouldGetPlayerSelectionForFlexRecoveringFromFailure() = runTest {
        runBlocking {
            Mockito.`when`(getOnlyPlayersInFieldUseCase(any()))
                .thenReturn(Result.failure(NoSuchElementException("none")))
        }

        val result = viewModel.getPlayerSelectionForFlex()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<PlayerWithPosition>(), result.getOrNull())
    }

    @Test
    fun shouldLinkDpAndFlexOnSuccess() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val dp = Player(id = 1L, teamId = 1L, name = "dp", shirtNumber = 1, licenseNumber = "1")
        runBlocking {
            Mockito.`when`(saveDpAndFlexUseCase(eq(lineup), eq(dp), eq(null), any()))
                .thenAnswer(returnsPlayersArgument(3))
        }

        viewModel.onDpFlexLinked(dp, null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dpFlexEvent)
    }

    @Test
    fun shouldShowInfoMessageWhenBothPlayersMissingForDpFlex() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(saveDpAndFlexUseCase(eq(lineup), eq(null), eq(null), any()))
                .thenReturn(Result.failure(NeedAssignBothPlayersException()))
        }

        val events = collectEvents(viewModel.events)

        viewModel.onDpFlexLinked(null, null)
        advanceUntilIdle()

        assertEquals(listOf(LineupEvent.DpFlexIncomplete), events)
        assertEquals(
            LineupInfoMessage.BothDpAndFlexRequired,
            viewModel.uiState.value.infoMessage
        )
    }

    @Test
    fun shouldNotLinkDpAndFlexWhenLineupIsNull() = runTest {
        viewModel.onDpFlexLinked(null, null)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(saveDpAndFlexUseCase)
    }

    @Test
    fun shouldUpdateBattersThroughUseCase() = runTest {
        runBlocking {
            Mockito.`when`(updatePlayersWithBattersUseCase(any(), any()))
                .thenAnswer(returnsPlayersArgument(0))
        }

        viewModel.onBattersChanged(emptyList())
        advanceUntilIdle()

        runBlocking {
            Mockito.verify(updatePlayersWithBattersUseCase)(any(), any())
        }
    }

    private suspend fun openRosterSheet(lineup: Lineup, roster: TeamRosterSummary) {
        loadLineup(lineup)
        Mockito.`when`(getRosterUseCase(lineup.id)).thenReturn(Result.success(roster))
        viewModel.onEditRosterClick()
    }

    @Test
    fun shouldLoadAndMutateRosterEditState() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = emptyList()
        )

        openRosterSheet(lineup, roster)

        val state = viewModel.uiState.value
        assertTrue(state.showRosterEditSheet)
        assertEquals(roster, state.rosterEditOriginalSummary)
    }

    @Test
    fun shouldDismissImmediatelyWhenRosterEditUnchanged() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = emptyList()
        )
        openRosterSheet(lineup, roster)

        viewModel.onRosterEditDismissRequested()

        val state = viewModel.uiState.value
        assertFalse(state.showRosterEditSheet)
        assertFalse(state.showRosterEditDiscardSheet)
    }

    @Test
    fun shouldShowDiscardSheetWhenRosterEditChanged() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = listOf(
                com.telen.easylineup.domain.model.RosterPlayerStatus(
                    player = Player(
                        id = 1L, teamId = 1L, name = "p1", shirtNumber = 1, licenseNumber = "1"
                    ),
                    status = true,
                    playerNumberOverlay = null
                )
            )
        )
        openRosterSheet(lineup, roster)

        viewModel.onRosterEditPlayerToggled(0, false)
        viewModel.onRosterEditDismissRequested()

        assertTrue(viewModel.uiState.value.showRosterEditDiscardSheet)
    }

    @Test
    fun shouldCloseRosterEditSheet() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = emptyList()
        )
        openRosterSheet(lineup, roster)

        viewModel.closeRosterEditSheet()

        val state = viewModel.uiState.value
        assertFalse(state.showRosterEditSheet)
        assertFalse(state.showRosterEditDiscardSheet)
    }

    @Test
    fun shouldSelectTab() {
        viewModel.onTabSelected(1)

        assertEquals(1, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun shouldSwitchTwoPlayersPositionsAndRecompute() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val first = player(1L, position = FieldPosition.PITCHER.id)
        val second = player(2L, position = FieldPosition.CATCHER.id)
        Mockito.`when`(switchPlayersPositionUseCase(any(), any(), any(), any()))
            .thenAnswer(returnsPlayersArgument(0))
        loadLineup(lineup, listOf(first, second))

        viewModel.switchPlayersPosition(first, second)
        advanceUntilIdle()

        Mockito.verify(switchPlayersPositionUseCase)(
            any(),
            eq(FieldPosition.PITCHER),
            eq(FieldPosition.CATCHER),
            eq(lineup)
        )
    }

    @Test
    fun shouldSwitchAPlayerToAnEmptyPosition() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val first = player(1L, position = FieldPosition.PITCHER.id)
        Mockito.`when`(switchPlayersPositionUseCase(any(), any(), any(), any()))
            .thenAnswer(returnsPlayersArgument(0))
        loadLineup(lineup, listOf(first))

        viewModel.switchPlayersPosition(first, FieldPosition.LEFT_FIELD)
        advanceUntilIdle()

        Mockito.verify(switchPlayersPositionUseCase)(
            any(),
            eq(FieldPosition.PITCHER),
            eq(FieldPosition.LEFT_FIELD),
            eq(lineup)
        )
    }

    @Test
    fun shouldDoNothingWhenSwitchingBeforeTheLineupIsLoaded() = runTest {
        val first = player(1L, position = FieldPosition.PITCHER.id)

        viewModel.switchPlayersPosition(first, FieldPosition.LEFT_FIELD)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(switchPlayersPositionUseCase)
    }

    private fun rosterOfOnePlayer() = TeamRosterSummary(
        status = com.telen.easylineup.domain.Constants.STATUS_ALL,
        players = listOf(
            com.telen.easylineup.domain.model.RosterPlayerStatus(
                player = Player(
                    id = 1L, teamId = 1L, name = "p1", shirtNumber = 1, licenseNumber = "1"
                ),
                status = true,
                playerNumberOverlay = null
            )
        )
    )

    @Test
    fun shouldOverrideARosterPlayerNumberForTheEditedLineup() = runTest {
        val lineup = Lineup(id = 7L, name = "L", teamId = 1L, tournamentId = 1L)
        openRosterSheet(lineup, rosterOfOnePlayer())

        viewModel.onRosterEditNumberChanged(0, 42)

        val overlay = viewModel.uiState.value.rosterEditSummary.players.single()
            .playerNumberOverlay
        assertEquals(42, overlay?.number)
        assertEquals(7L, overlay?.lineupId)
    }

    @Test
    fun shouldDeselectEveryPlayerWhenClearingTheRoster() = runTest {
        val lineup = Lineup(id = 7L, name = "L", teamId = 1L, tournamentId = 1L)
        openRosterSheet(lineup, rosterOfOnePlayer())

        viewModel.onRosterEditClearAll()

        assertFalse(viewModel.uiState.value.rosterEditSummary.players.single().status)
    }

    @Test
    fun shouldKeepEditingWhenDismissingTheDiscardSheet() = runTest {
        val lineup = Lineup(id = 7L, name = "L", teamId = 1L, tournamentId = 1L)
        openRosterSheet(lineup, rosterOfOnePlayer())
        viewModel.onRosterEditPlayerToggled(0, false)
        viewModel.onRosterEditDismissRequested()

        viewModel.onRosterEditDiscardSheetDismiss()

        val state = viewModel.uiState.value
        assertFalse(state.showRosterEditDiscardSheet)
        assertTrue(state.showRosterEditSheet)
    }

    private val loadedLineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
    private val aPlayer =
        Player(id = 1L, teamId = 1L, name = "p1", shirtNumber = 1, licenseNumber = "1")


    @Test
    fun shouldStillLoadTheLineupWhenTheTeamCannotBeRead() = runTest {
        Mockito.`when`(getTeamUseCase()).thenReturn(Result.failure(Exception("boom")))

        loadLineup(loadedLineup, listOf(player(1L)))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.defensePlayers.size)
    }

    @Test
    fun shouldKeepPreviousBattersWhenRecomputingFails() = runTest {
        Mockito.`when`(getBattersStateUseCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("boom")))

        loadLineup(loadedLineup, listOf(player(1L)))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.batters.isEmpty())
    }

    @Test
    fun shouldComputeBattersForEditModeWhenEditable() = runTest {
        editable = true

        loadLineup(loadedLineup, listOf(player(1L)))
        advanceUntilIdle()

        Mockito.verify(getBattersStateUseCase, Mockito.atLeastOnce())(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(true)
        )
    }

    @Test
    fun shouldDeleteAPositionWithDefaultsBeforeTheLineupIsLoaded() = runTest {
        Mockito.`when`(deletePlayerFieldPositionUseCase(any(), any(), any(), any()))
            .thenAnswer(returnsPlayersArgument(0))

        viewModel.onDeletePosition(aPlayer)
        advanceUntilIdle()

        Mockito.verify(deletePlayerFieldPositionUseCase)(any(), eq(aPlayer), eq(0), eq(0))
        Mockito.verify(getBattersStateUseCase, Mockito.never())(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun shouldNotRecomputeWhenDeletingAPositionFails() = runTest {
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.clearInvocations(getBattersStateUseCase)
        Mockito.`when`(deletePlayerFieldPositionUseCase(any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("boom")))

        viewModel.onDeletePosition(aPlayer)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(getBattersStateUseCase)
    }

    @Test
    fun shouldIgnoreLineupModeChangesBeforeTheLineupIsLoaded() = runTest {
        viewModel.onLineupModeChanged(true)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(setLineupModeUseCase)
    }

    @Test
    fun shouldKeepTheLineupModeWhenChangingItFails() = runTest {
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.`when`(setLineupModeUseCase(any(), any(), any()))
            .thenReturn(Result.failure(Exception("boom")))

        viewModel.onLineupModeChanged(true)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.lineupMode)
    }

    @Test
    fun shouldExportAnEmptyCardBeforeTheLineupIsLoaded() = runTest {
        val captor = argumentCaptor<LineupExportData>()
        Mockito.`when`(lineupPdfWriter.write(any())).thenReturn(Mockito.mock(Uri::class.java))

        viewModel.onShareClick()
        advanceUntilIdle()

        Mockito.verify(lineupPdfWriter).write(captor.capture())
        assertEquals("", captor.firstValue.lineupName)
        assertEquals(0L, captor.firstValue.eventTimeInMillis)
    }

    @Test
    fun shouldFallBackToFirstBaseForAPlayerWithoutAValidPosition() = runTest {
        Mockito.`when`(switchPlayersPositionUseCase(any(), any(), any(), any()))
            .thenAnswer(returnsPlayersArgument(0))
        loadLineup(loadedLineup)
        advanceUntilIdle()

        viewModel.switchPlayersPosition(player(1L), player(2L))
        viewModel.switchPlayersPosition(player(1L), FieldPosition.PITCHER)
        advanceUntilIdle()

        Mockito.verify(switchPlayersPositionUseCase)(
            any(),
            eq(FieldPosition.FIRST_BASE),
            eq(FieldPosition.FIRST_BASE),
            eq(loadedLineup)
        )
        Mockito.verify(switchPlayersPositionUseCase)(
            any(),
            eq(FieldPosition.FIRST_BASE),
            eq(FieldPosition.PITCHER),
            eq(loadedLineup)
        )
    }

    @Test
    fun shouldNotRecomputeWhenSwitchingPlayersFails() = runTest {
        Mockito.`when`(switchPlayersPositionUseCase(any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("boom")))
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.clearInvocations(getBattersStateUseCase)

        viewModel.switchPlayersPosition(player(1L), FieldPosition.PITCHER)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(getBattersStateUseCase)
    }

    @Test
    fun shouldCloseThePlayerPickerEvenBeforeTheLineupIsLoaded() = runTest {
        viewModel.onPlayerSelected(aPlayer, FieldPosition.PITCHER)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(assignPlayerFieldPositionUseCase)
        assertNull(viewModel.uiState.value.availablePlayersEvent)
    }

    @Test
    fun shouldNotRecomputeWhenAssigningAPlayerFails() = runTest {
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.clearInvocations(getBattersStateUseCase)
        Mockito.`when`(assignPlayerFieldPositionUseCase(any(), any(), any(), any()))
            .thenReturn(Result.failure(Exception("boom")))

        viewModel.onPlayerSelected(aPlayer, FieldPosition.PITCHER)
        advanceUntilIdle()

        Mockito.verifyNoInteractions(getBattersStateUseCase)
    }

    @Test
    fun shouldShowNothingWhenAvailablePlayersCannotBeRead() = runTest {
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.`when`(getListAvailablePlayersForSelectionUseCase(any(), any(), any()))
            .thenReturn(Result.failure(Exception("boom")))

        val result = viewModel.onPositionClicked(FieldPosition.PITCHER)

        assertTrue(result.isFailure)
        assertNull(viewModel.uiState.value.availablePlayersEvent)
        assertNull(viewModel.uiState.value.infoMessage)
    }

    @Test
    fun shouldShowNoMessageWhenTheDpFlexSheetFailsForAnotherReason() = runTest {
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.`when`(getDpAndFlexFromPlayersInFieldUseCase(any()))
            .thenReturn(Result.failure(Exception("boom")))

        val result = viewModel.onPositionClicked(FieldPosition.DP_DH)

        assertTrue(result.isFailure)
        assertNull(viewModel.uiState.value.infoMessage)
        assertNull(viewModel.uiState.value.dpFlexEvent)
    }

    @Test
    fun shouldOfferNoDpCandidatesBeforeTheLineupIsLoaded() = runTest {
        assertEquals(
            emptyList<PlayerWithPosition>(),
            viewModel.getPlayerSelectionForDp().getOrThrow()
        )
    }

    @Test
    fun shouldReturnDpAndFlexCandidates() = runTest {
        val candidates = listOf(player(1L))
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.`when`(getListAvailablePlayersForSelectionUseCase(any(), eq(null), any()))
            .thenReturn(Result.success(candidates))
        Mockito.`when`(getOnlyPlayersInFieldUseCase(any())).thenReturn(Result.success(candidates))

        assertEquals(candidates, viewModel.getPlayerSelectionForDp().getOrThrow())
        assertEquals(candidates, viewModel.getPlayerSelectionForFlex().getOrThrow())
    }

    @Test
    fun shouldOfferNoFlexCandidatesOnAnyOtherFailure() = runTest {
        Mockito.`when`(getOnlyPlayersInFieldUseCase(any()))
            .thenReturn(Result.failure(IllegalStateException("boom")))

        assertEquals(
            emptyList<PlayerWithPosition>(),
            viewModel.getPlayerSelectionForFlex().getOrThrow()
        )
    }

    @Test
    fun shouldShowNoMessageWhenLinkingFailsForAnotherReason() = runTest {
        loadLineup(loadedLineup)
        advanceUntilIdle()
        Mockito.`when`(saveDpAndFlexUseCase(any(), any(), any(), any()))
            .thenReturn(Result.failure(IllegalStateException("boom")))

        val events = collectEvents(viewModel.events)

        viewModel.onDpFlexLinked(aPlayer, aPlayer)
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        assertNull(viewModel.uiState.value.infoMessage)
    }

    @Test
    fun shouldSurviveAFailureWhenUpdatingBatters() = runTest {
        Mockito.`when`(updatePlayersWithBattersUseCase(any(), any()))
            .thenReturn(Result.failure(Exception("boom")))

        viewModel.onBattersChanged(emptyList())
        advanceUntilIdle()

        Mockito.verify(updatePlayersWithBattersUseCase)(any(), any())
    }

    private suspend fun startEditingAfterADelete() {
        editable = true
        loadLineup(loadedLineup, listOf(player(1L, FieldPosition.PITCHER.id, 1)))
        Mockito.`when`(deletePlayerFieldPositionUseCase(any(), any(), any(), any()))
            .thenAnswer(returnsPlayersArgument(0))
        viewModel.onDeletePosition(aPlayer)
    }

    @Test
    fun shouldKeepAnUnsavedEditWhenTheDatabaseEmitsAgain() = runTest {
        startEditingAfterADelete()
        advanceUntilIdle()

        playersFlow.emit(listOf(player(9L)))
        advanceUntilIdle()

        assertEquals(listOf(1L), viewModel.uiState.value.defensePlayers.map { it.playerId })
    }

    @Test
    fun shouldFollowTheDatabaseWhileNothingWasEdited() = runTest {
        editable = true
        loadLineup(loadedLineup, listOf(player(1L)))
        advanceUntilIdle()

        playersFlow.emit(listOf(player(9L)))
        advanceUntilIdle()

        assertEquals(listOf(9L), viewModel.uiState.value.defensePlayers.map { it.playerId })
    }

    @Test
    fun shouldBuildEachChangeOnTheResultOfThePreviousOne() = runTest {
        editable = true
        loadLineup(loadedLineup, listOf(player(1L, FieldPosition.PITCHER.id, order = 1)))
        advanceUntilIdle()
        Mockito.`when`(updatePlayersWithBattersUseCase(any(), any())).thenAnswer {
            it.getArgument<List<PlayerWithPosition>>(0).map { p -> p.copy(order = p.order + 1) }
        }

        viewModel.onBattersChanged(emptyList())
        viewModel.onBattersChanged(emptyList())
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.defensePlayers.single().order)
    }

    @Test
    fun shouldIgnoreASaveWhileTheFirstOneIsStillRunning() = runTest {
        startEditingAfterADelete()
        advanceUntilIdle()
        Mockito.`when`(saveLineupDraftUseCase(any(), any(), any())).doSuspendableAnswer {
            delay(1_000)
            Result.success(Unit)
        }

        viewModel.onSaveClick()
        viewModel.onSaveClick()
        advanceUntilIdle()

        Mockito.verify(saveLineupDraftUseCase, Mockito.times(1)).invoke(any(), any(), any())
    }

    @Test
    fun shouldReadTheDatabaseAgainOnceTheEditIsSaved() = runTest {
        startEditingAfterADelete()
        Mockito.`when`(saveLineupDraftUseCase(eq(loadedLineup), any(), any()))
            .thenReturn(Result.success(Unit))
        viewModel.onSaveClick()
        advanceUntilIdle()

        playersFlow.emit(listOf(player(9L)))
        advanceUntilIdle()

        assertEquals(listOf(9L), viewModel.uiState.value.defensePlayers.map { it.playerId })
    }


    private fun rosterOf(vararg selected: Pair<Long, Boolean>) = TeamRosterSummary(
        status = com.telen.easylineup.domain.Constants.STATUS_ALL,
        players = selected.map { (id, status) ->
            com.telen.easylineup.domain.model.RosterPlayerStatus(
                player = Player(
                    id = id,
                    teamId = 1L,
                    name = "p$id",
                    shirtNumber = id.toInt(),
                    licenseNumber = "$id"
                ),
                status = status,
                playerNumberOverlay = null
            )
        }
    )

    /** Edit mode, pitcher 1 and catcher 2 on the field, roster sheet open with both picked. */
    private suspend fun openRosterSheetWhileEditing() {
        editable = true
        val pitcher = player(1L, FieldPosition.PITCHER.id, 1)
        val catcher = player(2L, FieldPosition.CATCHER.id, 2)
        loadLineup(loadedLineup, listOf(pitcher, catcher))
        Mockito.`when`(getRosterUseCase(loadedLineup.id))
            .thenReturn(Result.success(rosterOf(1L to true, 2L to true)))
        viewModel.onEditRosterClick()
    }

    @Test
    fun shouldApplyAnEditedRosterToTheDraftWithoutSavingIt() = runTest {
        openRosterSheetWhileEditing()

        viewModel.onRosterEditPlayerToggled(1, false)
        viewModel.onRosterEditDone()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showRosterEditSheet)
        val catcher = state.defensePlayers.single { it.playerId == 2L }
        assertFalse(catcher.isAssigned())
        val pitcher = state.defensePlayers.single { it.playerId == 1L }
        assertEquals(FieldPosition.PITCHER.id, pitcher.position)
        Mockito.verifyNoInteractions(saveLineupDraftUseCase)
    }

    @Test
    fun shouldKeepTheDraftAfterARosterChangeInsteadOfReReadingTheDatabase() = runTest {
        openRosterSheetWhileEditing()
        viewModel.onRosterEditPlayerToggled(1, false)
        viewModel.onRosterEditDone()
        advanceUntilIdle()

        playersFlow.emit(listOf(player(9L)))
        advanceUntilIdle()

        assertEquals(
            listOf(1L, 2L),
            viewModel.uiState.value.defensePlayers.map { it.playerId }
        )
        Mockito.verify(observeLineupByIdUseCase, Mockito.times(1)).invoke(loadedLineup.id)
    }

    @Test
    fun shouldReopenTheRosterSheetOnTheRosterAlreadyApplied() = runTest {
        openRosterSheetWhileEditing()
        viewModel.onRosterEditPlayerToggled(1, false)
        viewModel.onRosterEditDone()
        advanceUntilIdle()

        viewModel.onEditRosterClick()

        val state = viewModel.uiState.value
        assertTrue(state.showRosterEditSheet)
        assertFalse(state.rosterEditSummary.players.single { it.player.id == 2L }.status)
        Mockito.verify(getRosterUseCase, Mockito.times(1)).invoke(loadedLineup.id)
    }

    @Test
    fun shouldSaveTheAppliedRosterAlongWithTheLineup() = runTest {
        openRosterSheetWhileEditing()
        viewModel.onRosterEditPlayerToggled(1, false)
        viewModel.onRosterEditDone()
        advanceUntilIdle()
        Mockito.`when`(saveLineupDraftUseCase(any(), any(), any()))
            .thenReturn(Result.success(Unit))

        viewModel.onSaveClick()
        advanceUntilIdle()

        val savedLineup = argumentCaptor<Lineup>()
        val savedNumbers =
            argumentCaptor<List<com.telen.easylineup.domain.model.RosterPlayerStatus>>()
        Mockito.verify(saveLineupDraftUseCase)
            .invoke(savedLineup.capture(), any(), savedNumbers.capture())
        assertEquals("1", savedLineup.firstValue.roster)
        assertEquals(listOf(1L, 2L), savedNumbers.firstValue.map { it.player.id })
    }

    @Test
    fun shouldLeaveTheDraftUntouchedWhenTheRosterIsUnchanged() = runTest {
        openRosterSheetWhileEditing()

        viewModel.onRosterEditDone()
        advanceUntilIdle()

        // Nothing was edited, so the draft still follows the database.
        assertFalse(viewModel.uiState.value.showRosterEditSheet)
        playersFlow.emit(listOf(player(9L)))
        advanceUntilIdle()
        assertEquals(listOf(9L), viewModel.uiState.value.defensePlayers.map { it.playerId })
    }

    @Test
    fun shouldApplyShirtNumberOverlaysWithoutTouchingTheEmittedPlayers() = runTest {
        val emitted = player(1L)
        loadLineup(
            loadedLineup,
            listOf(emitted),
            listOf(PlayerNumberOverlay(lineupId = 1L, playerId = 1L, number = 99))
        )
        advanceUntilIdle()

        assertEquals(99, viewModel.uiState.value.defensePlayers.single().shirtNumber)
        assertEquals(1, emitted.shirtNumber)
    }
}
