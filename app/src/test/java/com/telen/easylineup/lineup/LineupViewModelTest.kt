/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveLineupById
import com.telen.easylineup.domain.usecases.ObservePlayerNumberOverlays
import com.telen.easylineup.domain.usecases.ObserveTeamPlayersAndMaybePositionsForLineup
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
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
    @Mock lateinit var saveBattingOrderAndPositionsUseCase: SaveBattingOrderAndPositions
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
    @Mock lateinit var context: Context

    private lateinit var viewModel: LineupViewModel

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

        viewModel = buildViewModel()
    }

    private fun buildViewModel(): LineupViewModel = LineupViewModel(
        getTeamUseCase,
        observePlayerNumberOverlays,
        deletePlayerFieldPositionUseCase,
        switchPlayersPositionUseCase,
        assignPlayerFieldPositionUseCase,
        getListAvailablePlayersForSelectionUseCase,
        saveBattingOrderAndPositionsUseCase,
        deleteLineupUseCase,
        setLineupModeUseCase,
        getDpAndFlexFromPlayersInFieldUseCase,
        getBattersStateUseCase,
        observeTeamPlayersUseCase,
        observeLineupByIdUseCase,
        getOnlyPlayersInFieldUseCase,
        saveDpAndFlexUseCase,
        updatePlayersWithBattersUseCase,
        ToggleRosterPlayer(),
        ClearRoster(),
        OverrideRosterPlayerNumber(),
        lineupPdfWriter,
        testDispatcherProvider()
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun loadLineup(
        lineup: Lineup,
        players: List<PlayerWithPosition> = emptyList(),
        overlays: List<PlayerNumberOverlay> = emptyList()
    ) {
        viewModel.lineupId = lineup.id
        lineupFlow.emit(lineup)
        playersFlow.emit(players)
        overlaysFlow.emit(overlays)
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
            ).thenReturn(Result.success(Unit))
        }

        viewModel.onDeletePosition(
            Player(id = 1L, teamId = 1L, name = "p1", shirtNumber = 1, licenseNumber = "1")
        )
        advanceUntilIdle()

        runBlocking {
            Mockito.verify(deletePlayerFieldPositionUseCase)(any(), any(), any(), any())
        }
    }

    @Test
    fun shouldFailSaveWhenLineupIsNull() = runTest {
        val result = viewModel.save()

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldSaveSuccessfullyWhenLineupIsLoaded() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(saveBattingOrderAndPositionsUseCase(eq(lineup), any()))
                .thenReturn(Result.success(Unit))
        }

        val result = viewModel.save()

        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldDeleteLineupThroughUseCase() = runTest {
        viewModel.lineupId = 1L
        runBlocking {
            Mockito.`when`(deleteLineupUseCase(1L)).thenReturn(Result.success(Unit))
        }

        val result = viewModel.deleteLineup()

        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldUpdateLineupModeOnSuccess() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val updatedLineup = lineup.copy(mode = 1)
        runBlocking {
            Mockito.`when`(setLineupModeUseCase(eq(true), eq(lineup), any()))
                .thenReturn(Result.success(updatedLineup))
        }

        viewModel.onLineupModeChanged(true)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.lineupMode)
    }

    @Test
    fun shouldExportLineupToIntent() = runTest {
        val lineup = Lineup(id = 1L, name = "My Lineup", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup)
        advanceUntilIdle()
        val uri = Mockito.mock(Uri::class.java)
        Mockito.`when`(context.getString(Mockito.anyInt(), any())).thenReturn("Share subject")
        Mockito.`when`(lineupPdfWriter.write(any())).thenReturn(uri)

        // android.content.Intent's own methods aren't implemented on the unit test JVM -
        // mockConstruction swaps the real Intent() for a plain mock so putExtra/setType/etc.
        // don't throw "not mocked".
        Mockito.mockConstruction(Intent::class.java) { mock, _ ->
            Mockito.`when`(mock.type).thenReturn("application/pdf")
        }.use {
            val intent = viewModel.exportLineupToExternalStorage(context)

            assertEquals("application/pdf", intent.type)
        }
    }

    @Test
    fun shouldAssignPlayerAndClearAvailablePlayersEvent() = runTest {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        loadLineup(lineup, listOf(player(2L)))
        advanceUntilIdle()
        runBlocking {
            Mockito.`when`(assignPlayerFieldPositionUseCase(any(), any(), eq(lineup), any()))
                .thenReturn(Result.success(Unit))
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
            ).thenReturn(Result.success(Unit))
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
                .thenReturn(Result.success(Unit))
        }

        val result = viewModel.linkDpAndFlex(dp, null)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
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

        val result = viewModel.linkDpAndFlex(null, null)

        assertTrue(result.isFailure)
        assertEquals(
            LineupInfoMessage.BothDpAndFlexRequired,
            viewModel.uiState.value.infoMessage
        )
    }

    @Test
    fun shouldFailLinkDpAndFlexWhenLineupIsNull() = runTest {
        val result = viewModel.linkDpAndFlex(null, null)

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldUpdateBattersThroughUseCase() = runTest {
        runBlocking {
            Mockito.`when`(updatePlayersWithBattersUseCase(any(), any()))
                .thenReturn(Result.success(Unit))
        }

        viewModel.onBattersChanged(emptyList())
        advanceUntilIdle()

        runBlocking {
            Mockito.verify(updatePlayersWithBattersUseCase)(any(), any())
        }
    }

    @Test
    fun shouldLoadAndMutateRosterEditState() {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = emptyList()
        )

        viewModel.onRosterEditLoaded(lineup, roster)

        val state = viewModel.uiState.value
        assertTrue(state.showRosterEditSheet)
        assertEquals(lineup, state.rosterEditLineup)
        assertEquals(roster, state.rosterEditOriginalSummary)
    }

    @Test
    fun shouldDismissImmediatelyWhenRosterEditUnchanged() {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = emptyList()
        )
        viewModel.onRosterEditLoaded(lineup, roster)

        viewModel.onRosterEditDismissRequested()

        val state = viewModel.uiState.value
        assertFalse(state.showRosterEditSheet)
        assertFalse(state.showRosterEditDiscardSheet)
    }

    @Test
    fun shouldShowDiscardSheetWhenRosterEditChanged() {
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
        viewModel.onRosterEditLoaded(lineup, roster)

        viewModel.onRosterEditPlayerToggled(0, false)
        viewModel.onRosterEditDismissRequested()

        assertTrue(viewModel.uiState.value.showRosterEditDiscardSheet)
    }

    @Test
    fun shouldCloseRosterEditSheet() {
        val lineup = Lineup(id = 1L, name = "L", teamId = 1L, tournamentId = 1L)
        val roster = TeamRosterSummary(
            status = com.telen.easylineup.domain.Constants.STATUS_ALL,
            players = emptyList()
        )
        viewModel.onRosterEditLoaded(lineup, roster)

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
            .thenReturn(Result.success(Unit))
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
            .thenReturn(Result.success(Unit))
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
    fun shouldOverrideARosterPlayerNumberForTheEditedLineup() {
        val lineup = Lineup(id = 7L, name = "L", teamId = 1L, tournamentId = 1L)
        viewModel.onRosterEditLoaded(lineup, rosterOfOnePlayer())

        viewModel.onRosterEditNumberChanged(0, 42)

        val overlay = viewModel.uiState.value.rosterEditSummary.players.single()
            .playerNumberOverlay
        assertEquals(42, overlay?.number)
        assertEquals(7L, overlay?.lineupId)
    }

    @Test
    fun shouldDeselectEveryPlayerWhenClearingTheRoster() {
        val lineup = Lineup(id = 7L, name = "L", teamId = 1L, tournamentId = 1L)
        viewModel.onRosterEditLoaded(lineup, rosterOfOnePlayer())

        viewModel.onRosterEditClearAll()

        assertFalse(viewModel.uiState.value.rosterEditSummary.players.single().status)
    }

    @Test
    fun shouldKeepEditingWhenDismissingTheDiscardSheet() {
        val lineup = Lineup(id = 7L, name = "L", teamId = 1L, tournamentId = 1L)
        viewModel.onRosterEditLoaded(lineup, rosterOfOnePlayer())
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
    fun shouldNotObserveAnythingWithoutALineupId() {
        viewModel.lineupId = null

        Mockito.verifyNoInteractions(observeLineupByIdUseCase)
    }

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
        viewModel.editable = true

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
            .thenReturn(Result.success(Unit))

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
        Mockito.`when`(context.getString(Mockito.anyInt(), any())).thenReturn("Share subject")
        Mockito.`when`(lineupPdfWriter.write(any())).thenReturn(Mockito.mock(Uri::class.java))

        Mockito.mockConstruction(Intent::class.java).use {
            viewModel.exportLineupToExternalStorage(context)
        }

        Mockito.verify(lineupPdfWriter).write(captor.capture())
        assertEquals("", captor.firstValue.lineupName)
        assertEquals(0L, captor.firstValue.eventTimeInMillis)
    }

    @Test
    fun shouldFallBackToFirstBaseForAPlayerWithoutAValidPosition() = runTest {
        Mockito.`when`(switchPlayersPositionUseCase(any(), any(), any(), any()))
            .thenReturn(Result.success(Unit))
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

        val result = viewModel.linkDpAndFlex(aPlayer, aPlayer)

        assertTrue(result.isFailure)
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
}
