/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.lineup

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
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
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SaveLineupDraft
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.domain.usecases.runCatchingCancellable
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.LineupExportData
import com.telen.easylineup.utils.LineupPdfWriter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

sealed class EventCase

/**
 * @property initialData
 * @property dpLocked
 * @property flexLocked
 * @property teamType the sheet's own title depends only on this, so it's derived where the
 * string is actually resolved ([LineupRoute]) rather than carried here as a resource id.
 */
data class LinkDpFlex(
    val initialData: Pair<PlayerWithPosition?, PlayerWithPosition?>,
    val dpLocked: Boolean,
    val flexLocked: Boolean,
    val teamType: Int
) : EventCase()

/** Which of [LineupScreen]'s info dialogs to show - [LineupRoute] resolves the actual string. */
sealed class LineupInfoMessage {
    data object NoCandidatesForPosition : LineupInfoMessage()
    data object PitcherAssignmentRequired : LineupInfoMessage()
    data object BothDpAndFlexRequired : LineupInfoMessage()
}

/**
 * @property players
 * @property position
 */
data class ListAvailablePlayers(
    val players: List<PlayerWithPosition>,
    val position: FieldPosition
) : EventCase()

/**
 * One-shot outcomes [LineupRoute] acts on - navigation, the share sheet, a toast, analytics.
 * The work behind each one runs in [LineupViewModel]'s own scope, so leaving the screen or
 * rotating it never cancels a save or a delete halfway.
 */
sealed interface LineupEvent {
    data object Saved : LineupEvent

    /** Nothing was written (one transaction): the draft is still there to save again. */
    data object SaveFailed : LineupEvent
    data object Deleted : LineupEvent
    data object DeleteFailed : LineupEvent

    /** The PDF is written; the Route builds the share Intent with its own Context. */
    data class ShareReady(val pdfUri: Uri, val lineupName: String) : LineupEvent
    data object ShareFailed : LineupEvent

    /** The DP/Flex link was refused for lacking one of the two players. */
    data object DpFlexIncomplete : LineupEvent
}

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

/** Everything [LineupScreen] and its sheets need to render. */
data class LineupUiState(
    val isLoading: Boolean = true,

    /**
     * The lineup could not be read at all: the Route shows a retry state rather than an empty
     * field. A read failing once it was on screen keeps what is shown, unsaved edits included.
     */
    val loadFailed: Boolean = false,
    val lineupName: String = "",
    val defensePlayers: ImmutableList<PlayerWithPosition> = persistentListOf(),
    val defenseStrategy: TeamStrategy = TeamStrategy.STANDARD,
    val lineupMode: Int = MODE_DISABLED,
    val teamType: Int = TeamType.UNKNOWN.id,
    val extraHitters: Int = 0,
    val batters: ImmutableList<BatterState> = persistentListOf(),
    val selectedTab: Int = FRAGMENT_DEFENSE_INDEX,
    val availablePlayersEvent: ListAvailablePlayers? = null,
    val dpFlexEvent: LinkDpFlex? = null,
    val deleteConfirmPlayer: Player? = null,
    val infoMessage: LineupInfoMessage? = null,
    val showDiscardSheet: Boolean = false,
    val showDeleteSheet: Boolean = false,
    val rosterEditSummary: TeamRosterSummary = TeamRosterSummary(Constants.STATUS_ALL, emptyList()),
    val rosterEditOriginalSummary: TeamRosterSummary? = null,
    val showRosterEditSheet: Boolean = false,
    val showRosterEditDiscardSheet: Boolean = false
)

class LineupViewModel(
    savedStateHandle: SavedStateHandle,
    private val getTeamUseCase: GetTeam,
    private val observePlayerNumberOverlays: ObservePlayerNumberOverlays,
    private val deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition,
    private val switchPlayersPositionUseCase: SwitchPlayersPosition,
    private val assignPlayerFieldPositionUseCase: AssignPlayerFieldPosition,
    private val getListAvailablePlayersForSelectionUseCase: GetListAvailablePlayersForSelection,
    private val saveLineupDraftUseCase: SaveLineupDraft,
    private val deleteLineupUseCase: DeleteLineup,
    private val setLineupModeUseCase: SetLineupMode,
    private val getDpAndFlexFromPlayersInFieldUseCase: GetDpAndFlexFromPlayersInField,
    private val getBattersStateUseCase: GetBattersState,
    private val observeTeamPlayersAndMaybePositionsForLineupUseCase:
    ObserveTeamPlayersAndMaybePositionsForLineup,
    private val observeLineupByIdUseCase: ObserveLineupById,
    private val getOnlyPlayersInFieldUseCase: GetOnlyPlayersInField,
    private val saveDpAndFlexUseCase: SaveDpAndFlex,
    private val updatePlayersWithBattersUseCase: UpdatePlayersWithBatters,
    private val rosterUseCases: LineupRosterUseCases,
    private val lineupPdfWriter: LineupPdfWriter,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupUiState())
    val uiState: StateFlow<LineupUiState> = _uiState.asStateFlow()

    private val _events = Channel<LineupEvent>(Channel.BUFFERED)
    val events: Flow<LineupEvent> = _events.receiveAsFlow()
    private var shareJob: Job? = null

    private val _listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()
    private var teamTypeId: Int = TeamType.UNKNOWN.id
    private var observeJob: Job? = null

    var lineup: Lineup? = null
        private set

    /**
     * Which lineup this screen shows, and whether it edits it, read once from the screen's own
     * navigation arguments: they never change for a given back stack entry, and a rotation
     * keeps this ViewModel, so the observation below starts once and is never restarted under
     * an edit in progress. Read by the route's own property names, which Navigation uses as
     * argument keys, rather than through `toRoute()`, which needs a real Bundle.
     */
    val lineupId: Long = checkNotNull(savedStateHandle[Routes.Lineup::lineupId.name])
    val editable: Boolean = savedStateHandle[Routes.Lineup::editable.name] ?: false

    /**
     * Whether the user changed the lineup since it was last read from the database. While it
     * is, an edit session ignores what the database emits, which would otherwise overwrite the
     * unsaved changes: any write to the observed tables makes Room re-emit.
     */
    private var hasUnsavedChanges = false

    /**
     * The roster as edited during this session, once applied to the draft. Until the lineup is
     * saved it exists only here: reopening the roster sheet starts from it, and saving writes
     * its per-lineup shirt numbers along with the positions.
     */
    private var appliedRoster: TeamRosterSummary? = null

    init {
        observeLineup()
    }

    private fun observeLineup() {
        val id = lineupId

        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess {
                    teamTypeId = it.type
                    _uiState.update { state -> state.copy(teamType = it.type) }
                }
                .onFailure { Timber.e(it) }
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeLineupByIdUseCase(id)
                .flatMapLatest { loadedLineup ->
                    observeTeamPlayersAndMaybePositionsForLineupUseCase(loadedLineup.id)
                        .flatMapLatest { positions ->
                            observePlayerNumberOverlays(id)
                                .map { overlays -> Triple(loadedLineup, positions, overlays) }
                        }
                }
                .catch {
                    Timber.e(it)
                    if (lineup == null) {
                        _uiState.update { state ->
                            state.copy(isLoading = false, loadFailed = true)
                        }
                    }
                }
                .collect { (loadedLineup, positions, overlays) ->
                    if (editable && hasUnsavedChanges) {
                        Timber.d("Lineup $id changed in the database, keeping the unsaved edit")
                        return@collect
                    }
                    applyDatabaseSnapshot(loadedLineup, positions, overlays)
                }
        }
    }

    private suspend fun applyDatabaseSnapshot(
        loadedLineup: Lineup,
        positions: List<PlayerWithPosition>,
        overlays: List<PlayerNumberOverlay>
    ) {
        lineup = loadedLineup
        _uiState.update {
            it.copy(
                lineupName = loadedLineup.name,
                defenseStrategy = TeamStrategy.getStrategyById(loadedLineup.strategy),
                lineupMode = loadedLineup.mode,
                extraHitters = loadedLineup.extraHitters
            )
        }
        // Copies, so the draft never shares instances with what Room handed out.
        val draft = positions.map { it.copy() }
        val byPlayerId = draft.associateBy { it.playerId }
        overlays.forEach { overlay -> byPlayerId[overlay.playerId]?.shirtNumber = overlay.number }
        _listPlayersWithPosition.clear()
        _listPlayersWithPosition.addAll(draft)
        recomputeAndPublish()
    }

    /** Called synchronously, before the change is launched, by every action editing the draft. */
    private fun markUnsaved() {
        hasUnsavedChanges = true
    }

    /**
     * Snapshots [_listPlayersWithPosition] into [LineupUiState.defensePlayers] and recomputes
     * the batting order from it - called both after a fresh DB load and after every local
     * mutation below, since those mutate [_listPlayersWithPosition] in place rather than
     * going through another DB round-trip.
     */
    private suspend fun recomputeAndPublish() {
        // PlayerWithPosition instances are mutated in place by the domain use cases and the
        // same backing list is re-emitted - snapshot each element via .copy() so Compose's
        // structural-equality state check actually sees a change instead of comparing the
        // same mutated objects against themselves.
        _uiState.update {
            val snapshot = _listPlayersWithPosition.map { p -> p.copy() }.toImmutableList()
            it.copy(isLoading = false, defensePlayers = snapshot)
        }
        val currentLineup = lineup ?: return
        val batterSize = TeamStrategy.getStrategyById(currentLineup.strategy).batterSize
        getBattersStateUseCase(
            players = _listPlayersWithPosition,
            teamType = teamTypeId,
            batterSize = batterSize,
            extraHitterSize = currentLineup.extraHitters,
            isDebug = BuildConfig.DEBUG,
            isEditable = editable
        )
            .onSuccess { batters ->
                _uiState.update { it.copy(batters = batters.toImmutableList()) }
            }
            .onFailure { Timber.e(it) }
    }

    fun onRetryLoad() {
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        observeLineup()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    private suspend fun getNotSelectedPlayers(
        sortBy: FieldPosition?
    ): Result<List<PlayerWithPosition>> {
        val currentLineup = lineup
            ?: return Result.failure(IllegalArgumentException("Lineup is not expected to be null"))
        return getListAvailablePlayersForSelectionUseCase(
            _listPlayersWithPosition,
            sortBy,
            currentLineup
        )
    }

    fun onDeletePosition(player: Player) {
        markUnsaved()
        val lineupMode = lineup?.mode ?: MODE_DISABLED
        val hitters = lineup?.extraHitters ?: 0
        viewModelScope.launch {
            deletePlayerFieldPositionUseCase(_listPlayersWithPosition, player, lineupMode, hitters)
                .onSuccess { recomputeAndPublish() }
                .onFailure { Timber.e(it) }
        }
    }

    fun onSaveClick() {
        val currentLineup = lineup ?: run {
            Timber.e(IllegalStateException("Cannot save: the lineup is not loaded"))
            return
        }
        viewModelScope.launch {
            saveLineupDraftUseCase(
                currentLineup,
                _listPlayersWithPosition,
                appliedRoster?.players.orEmpty()
            )
                .onSuccess {
                    hasUnsavedChanges = false
                    appliedRoster = null
                    _events.send(LineupEvent.Saved)
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(LineupEvent.SaveFailed)
                }
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            deleteLineupUseCase(lineupId)
                .onSuccess { _events.send(LineupEvent.Deleted) }
                .onFailure {
                    Timber.e(it)
                    _events.send(LineupEvent.DeleteFailed)
                }
        }
    }

    fun onLineupModeChanged(isEnabled: Boolean) {
        lineup?.let { currentLineup ->
            markUnsaved()
            viewModelScope.launch {
                setLineupModeUseCase(isEnabled, currentLineup, _listPlayersWithPosition)
                    .onSuccess { updatedLineup ->
                        lineup = updatedLineup
                        _uiState.update { it.copy(lineupMode = updatedLineup.mode) }
                        recomputeAndPublish()
                    }
                    .onFailure { Timber.e(it) }
            }
        }
    }

    /** Writes the lineup card as a PDF, then hands it over as [LineupEvent.ShareReady]. */
    fun onShareClick() {
        shareJob?.cancel()
        shareJob = viewModelScope.launch {
            val currentLineup = lineup
            val state = _uiState.value
            val data = LineupExportData(
                lineupName = currentLineup?.name.orEmpty(),
                eventTimeInMillis = currentLineup?.eventTimeInMillis ?: 0L,
                teamType = state.teamType,
                strategy = state.defenseStrategy,
                battingOrder = state.batters,
                defensePlayers = state.defensePlayers
            )
            runCatchingCancellable {
                withContext(dispatcherProvider.io()) { lineupPdfWriter.write(data) }
            }
                .onSuccess { uri -> _events.send(LineupEvent.ShareReady(uri, data.lineupName)) }
                .onFailure {
                    Timber.e(it)
                    _events.send(LineupEvent.ShareFailed)
                }
        }
    }

    fun switchPlayersPosition(player1: PlayerWithPosition, player2: PlayerWithPosition) {
        val position1 = FieldPosition.getFieldPositionById(player1.position)
            ?: FieldPosition.FIRST_BASE
        val position2 = FieldPosition.getFieldPositionById(player2.position)
            ?: FieldPosition.FIRST_BASE
        switchPlayersPosition(position1, position2)
    }

    fun switchPlayersPosition(player1: PlayerWithPosition, position2: FieldPosition) {
        val position1 = FieldPosition.getFieldPositionById(player1.position)
            ?: FieldPosition.FIRST_BASE
        switchPlayersPosition(position1, position2)
    }

    private fun switchPlayersPosition(position1: FieldPosition, position2: FieldPosition) {
        val currentLineup = lineup ?: return
        markUnsaved()
        viewModelScope.launch {
            switchPlayersPositionUseCase(
                _listPlayersWithPosition,
                position1,
                position2,
                currentLineup
            )
                .onSuccess { recomputeAndPublish() }
                .onFailure { Timber.e("Cannot switch players: ${it.message}") }
        }
    }

    fun onPlayerSelected(player: Player, position: FieldPosition) {
        lineup?.let {
            markUnsaved()
            viewModelScope.launch {
                assignPlayerFieldPositionUseCase(player, position, it, _listPlayersWithPosition)
                    .onSuccess { recomputeAndPublish() }
                    .onFailure { Timber.e(it) }
            }
        }
        _uiState.update { it.copy(availablePlayersEvent = null) }
    }

    /**
     * Returns the failure (if any) so the Route can log the one analytics event that depends
     * on the exact exception type - every other outcome (sheet to show, empty-list message) is
     * already reflected in [uiState] by the time this returns.
     */
    suspend fun onPositionClicked(position: FieldPosition): Result<Unit> {
        return if (position != FieldPosition.DP_DH) {
            getNotSelectedPlayers(sortBy = position)
                .onSuccess { players ->
                    _uiState.update {
                        it.copy(
                            availablePlayersEvent = if (players.isEmpty()) {
                                null
                            } else {
                                ListAvailablePlayers(players, position)
                            },
                            infoMessage = if (players.isEmpty()) {
                                LineupInfoMessage.NoCandidatesForPosition
                            } else {
                                null
                            }
                        )
                    }
                }
                .onFailure { Timber.e(it) }
                .map {}
        } else {
            getDpAndFlexFromPlayersInFieldUseCase(_listPlayersWithPosition)
                .onSuccess {
                    val event = LinkDpFlex(
                        Pair(it.dp, it.flex),
                        it.dpLocked,
                        it.flexLocked,
                        it.teamType
                    )
                    _uiState.update { state -> state.copy(dpFlexEvent = event) }
                }
                .onFailure { throwable ->
                    if (throwable is NeedAssignPitcherFirstException) {
                        _uiState.update {
                            it.copy(infoMessage = LineupInfoMessage.PitcherAssignmentRequired)
                        }
                    } else {
                        Timber.e(throwable)
                    }
                }
                .map {}
        }
    }

    fun onAvailablePlayersDismiss() {
        _uiState.update { it.copy(availablePlayersEvent = null) }
    }

    fun onDpFlexDismiss() {
        _uiState.update { it.copy(dpFlexEvent = null) }
    }

    fun onSubstituteLongClicked(player: Player) {
        _uiState.update { it.copy(deleteConfirmPlayer = player) }
    }

    fun onDeleteConfirmDismiss() {
        _uiState.update { it.copy(deleteConfirmPlayer = null) }
    }

    fun onInfoMessageDismiss() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun onDiscardSheetRequested() {
        _uiState.update { it.copy(showDiscardSheet = true) }
    }

    fun onDiscardSheetDismiss() {
        _uiState.update { it.copy(showDiscardSheet = false) }
    }

    fun onDeleteSheetRequested() {
        _uiState.update { it.copy(showDeleteSheet = true) }
    }

    fun onDeleteSheetDismiss() {
        _uiState.update { it.copy(showDeleteSheet = false) }
    }

    suspend fun getPlayerSelectionForDp(): Result<List<PlayerWithPosition>> {
        return getNotSelectedPlayers(null).recoverCatching {
            Timber.e(it)
            listOf()
        }
    }

    suspend fun getPlayerSelectionForFlex(): Result<List<PlayerWithPosition>> {
        return getOnlyPlayersInFieldUseCase(_listPlayersWithPosition)
            .recoverCatching {
                if (it is NoSuchElementException) {
                    Timber.e(it.message.toString())
                } else {
                    Timber.e(it)
                }
                listOf()
            }
    }

    fun onDpFlexLinked(dp: Player?, flex: Player?) {
        val currentLineup = lineup ?: run {
            Timber.e(IllegalStateException("Cannot link DP and Flex: the lineup is not loaded"))
            return
        }
        markUnsaved()
        viewModelScope.launch {
            saveDpAndFlexUseCase(currentLineup, dp, flex, _listPlayersWithPosition)
                .onSuccess {
                    recomputeAndPublish()
                    _uiState.update { it.copy(dpFlexEvent = null) }
                }
                .onFailure { throwable ->
                    if (throwable is NeedAssignBothPlayersException) {
                        Timber.w(throwable.message)
                        _uiState.update {
                            it.copy(infoMessage = LineupInfoMessage.BothDpAndFlexRequired)
                        }
                        _events.send(LineupEvent.DpFlexIncomplete)
                    } else {
                        Timber.e(throwable)
                    }
                }
        }
    }

    fun onBattersChanged(batters: List<BatterState>) {
        markUnsaved()
        viewModelScope.launch {
            updatePlayersWithBattersUseCase(_listPlayersWithPosition, batters)
                .onFailure { Timber.e(it) }
        }
    }

    /**
     * Opens the roster sheet on the roster as this session last left it - the one already
     * applied to the draft, or else the saved one.
     */
    fun onEditRosterClick() {
        appliedRoster?.let {
            showRosterEditSheet(it)
            return
        }
        viewModelScope.launch {
            rosterUseCases.getRoster(lineupId)
                .onSuccess(::showRosterEditSheet)
                .onFailure { Timber.e(it) }
        }
    }

    private fun showRosterEditSheet(roster: TeamRosterSummary) {
        _uiState.update {
            it.copy(
                rosterEditSummary = roster,
                rosterEditOriginalSummary = roster,
                showRosterEditSheet = true
            )
        }
    }

    /**
     * Applies the edited roster to the draft, like any other change of this session: nothing is
     * written until the lineup is saved, and cancelling the edit drops it with the rest.
     */
    fun onRosterEditDone() {
        val state = _uiState.value
        val roster = state.rosterEditSummary
        val currentLineup = lineup
        if (roster == state.rosterEditOriginalSummary || currentLineup == null) {
            closeRosterEditSheet()
            return
        }
        markUnsaved()
        viewModelScope.launch {
            rosterUseCases.applyRosterToLineup(
                currentLineup,
                _listPlayersWithPosition,
                roster.players
            )
                .onSuccess { updatedLineup ->
                    lineup = updatedLineup
                    appliedRoster = roster
                    recomputeAndPublish()
                    closeRosterEditSheet()
                }
                .onFailure { Timber.e(it) }
        }
    }

    fun onRosterEditPlayerToggled(position: Int, checked: Boolean) {
        _uiState.update {
            it.copy(
                rosterEditSummary = rosterUseCases.toggleRosterPlayer(
                    it.rosterEditSummary,
                    position,
                    checked
                )
            )
        }
    }

    fun onRosterEditNumberChanged(position: Int, number: Int) {
        _uiState.update {
            val lineupId = lineup?.id ?: 0L
            it.copy(
                rosterEditSummary = rosterUseCases.overrideRosterPlayerNumber(
                    it.rosterEditSummary,
                    position,
                    number,
                    lineupId
                )
            )
        }
    }

    fun onRosterEditClearAll() {
        _uiState.update {
            it.copy(rosterEditSummary = rosterUseCases.clearRoster(it.rosterEditSummary))
        }
    }

    fun onRosterEditDiscardSheetDismiss() {
        _uiState.update { it.copy(showRosterEditDiscardSheet = false) }
    }

    fun onRosterEditDismissRequested() {
        val state = _uiState.value
        if (state.rosterEditSummary != state.rosterEditOriginalSummary) {
            _uiState.update { it.copy(showRosterEditDiscardSheet = true) }
        } else {
            closeRosterEditSheet()
        }
    }

    fun closeRosterEditSheet() {
        _uiState.update {
            it.copy(showRosterEditSheet = false, showRosterEditDiscardSheet = false)
        }
    }
}
