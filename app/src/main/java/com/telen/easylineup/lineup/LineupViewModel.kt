/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.lineup

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
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
import com.telen.easylineup.utils.LineupShareImageWriter
import com.telen.easylineup.views.LineupTypeface
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class EventCase

/**
 * @property initialData
 * @property dpLocked
 * @property flexLocked
 * @property teamType
 * @property title
 */
data class LinkDpFlex(
    val initialData: Pair<PlayerWithPosition?, PlayerWithPosition?>,
    val dpLocked: Boolean,
    val flexLocked: Boolean,
    val teamType: Int,
    @StringRes val title: Int
) : EventCase()

/**
 * @property players
 * @property position
 */
data class ListAvailablePlayers(
    val players: List<PlayerWithPosition>,
    val position: FieldPosition
) : EventCase()

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

/** Everything [LineupScreen] and its sheets need to render. */
data class LineupUiState(
    val isLoading: Boolean = true,
    val lineupName: String = "",
    val defensePlayers: List<PlayerWithPosition> = emptyList(),
    val defenseStrategy: TeamStrategy = TeamStrategy.STANDARD,
    val lineupMode: Int = MODE_DISABLED,
    val teamType: Int = TeamType.UNKNOWN.id,
    val extraHitters: Int = 0,
    val batters: List<BatterState> = emptyList(),
    val typeface: LineupTypeface = LineupTypeface.NORMAL,
    val selectedTab: Int = FRAGMENT_DEFENSE_INDEX,
    val availablePlayersEvent: ListAvailablePlayers? = null,
    val dpFlexEvent: LinkDpFlex? = null,
    val deleteConfirmPlayer: Player? = null,
    val infoMessageRes: Int? = null,
    val showDiscardSheet: Boolean = false,
    val showDeleteSheet: Boolean = false,
    val rosterEditLineup: Lineup? = null,
    val rosterEditSummary: TeamRosterSummary = TeamRosterSummary(Constants.STATUS_ALL, emptyList()),
    val rosterEditOriginalSummary: TeamRosterSummary? = null,
    val showRosterEditSheet: Boolean = false,
    val showRosterEditDiscardSheet: Boolean = false
)

class LineupViewModel(
    private val getTeamUseCase: GetTeam,
    private val observePlayerNumberOverlays: ObservePlayerNumberOverlays,
    private val deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition,
    private val switchPlayersPositionUseCase: SwitchPlayersPosition,
    private val assignPlayerFieldPositionUseCase: AssignPlayerFieldPosition,
    private val getListAvailablePlayersForSelectionUseCase: GetListAvailablePlayersForSelection,
    private val saveBattingOrderAndPositionsUseCase: SaveBattingOrderAndPositions,
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
    private val toggleRosterPlayerUseCase: ToggleRosterPlayer,
    private val clearRosterUseCase: ClearRoster,
    private val overrideRosterPlayerNumberUseCase: OverrideRosterPlayerNumber,
    private val lineupShareImageWriter: LineupShareImageWriter
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupUiState())
    val uiState: StateFlow<LineupUiState> = _uiState.asStateFlow()

    private val _listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()
    private var teamTypeId: Int = TeamType.UNKNOWN.id
    private var observeJob: Job? = null

    var lineup: Lineup? = null
        private set
    var lineupId: Long? = 0
        set(value) {
            field = value
            observeLineup()
        }
    var editable = false

    private fun observeLineup() {
        val id = lineupId ?: return

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
                .catch { Timber.e(it) }
                .flatMapLatest { loadedLineup ->
                    lineup = loadedLineup
                    _uiState.update {
                        it.copy(
                            lineupName = loadedLineup.name,
                            defenseStrategy = TeamStrategy.getStrategyById(loadedLineup.strategy),
                            lineupMode = loadedLineup.mode,
                            extraHitters = loadedLineup.extraHitters
                        )
                    }
                    observeTeamPlayersAndMaybePositionsForLineupUseCase(loadedLineup.id)
                        .catch { e -> Timber.e(e) }
                }
                .flatMapLatest { positions ->
                    _listPlayersWithPosition.clear()
                    _listPlayersWithPosition.addAll(positions)
                    val playerMap = mutableMapOf(
                        *positions.map { Pair(it.playerId, it) }.toTypedArray()
                    )
                    observePlayerNumberOverlays(id).catch { e -> Timber.e(e) }
                        .map { overlays ->
                            overlays.forEach { overlay ->
                                playerMap[overlay.playerId]?.shirtNumber = overlay.number
                            }
                            positions
                        }
                }
                .collect { recomputeAndPublish() }
        }
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
            it.copy(isLoading = false, defensePlayers = _listPlayersWithPosition.map { p -> p.copy() })
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
            .onSuccess { batters -> _uiState.update { it.copy(batters = batters) } }
            .onFailure { Timber.e(it) }
    }

    fun loadTypeface(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val lineupValue = preferences.getString(
            context.getString(R.string.key_lineup_style),
            context.getString(R.string.lineup_style_default_value)
        )
        _uiState.update { it.copy(typeface = LineupTypeface.getByValue(lineupValue)) }
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
        val lineupMode = lineup?.mode ?: MODE_DISABLED
        val hitters = lineup?.extraHitters ?: 0
        viewModelScope.launch {
            deletePlayerFieldPositionUseCase(_listPlayersWithPosition, player, lineupMode, hitters)
                .onSuccess { recomputeAndPublish() }
                .onFailure { Timber.e(it) }
        }
    }

    suspend fun save(): Result<Unit> {
        return lineup?.let {
            saveBattingOrderAndPositionsUseCase(it, _listPlayersWithPosition)
        } ?: Result.failure(IllegalArgumentException("Lineup is not supposed to be null"))
    }

    suspend fun deleteLineup(): Result<Unit> {
        return deleteLineupUseCase(lineupId)
    }

    fun onLineupModeChanged(isEnabled: Boolean) {
        lineup?.let { currentLineup ->
            viewModelScope.launch {
                setLineupModeUseCase(isEnabled, currentLineup, _listPlayersWithPosition)
                    .onSuccess {
                        lineup = currentLineup
                        _uiState.update { it.copy(lineupMode = currentLineup.mode) }
                        recomputeAndPublish()
                    }
                    .onFailure { Timber.e(it) }
            }
        }
    }

    fun exportLineupToExternalStorage(
        context: Context,
        views: Map<Int, Bitmap?>
    ): Single<Intent> {
        return Single.defer {
            val uris = ArrayList(lineupShareImageWriter.write(views))
            val title = lineup?.name.toString()
            val message = context.getString(R.string.share_lineup_subject, title)
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).run {
                putExtra(Intent.EXTRA_SUBJECT, message)
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            }
            Single.just(intent)
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
                            infoMessageRes = if (players.isEmpty()) {
                                R.string.players_list_empty
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
                    val title = if (it.teamType == TeamType.SOFTBALL.id) {
                        R.string.link_dp_and_flex_dialog_title
                    } else {
                        R.string.link_dh_and_pitcher_dialog_title
                    }
                    val event = LinkDpFlex(
                        Pair(it.dp, it.flex),
                        it.dpLocked,
                        it.flexLocked,
                        it.teamType,
                        title
                    )
                    _uiState.update { state -> state.copy(dpFlexEvent = event) }
                }
                .onFailure { throwable ->
                    if (throwable is NeedAssignPitcherFirstException) {
                        _uiState.update {
                            it.copy(infoMessageRes = R.string.error_need_assign_pitcher_first_message)
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
        _uiState.update { it.copy(infoMessageRes = null) }
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

    /** Returns the failure (if any) so the Route can log the exception-specific analytics. */
    suspend fun linkDpAndFlex(dp: Player?, flex: Player?): Result<Unit> {
        val currentLineup = lineup
            ?: return Result.failure(IllegalStateException("Lineup cannot be null"))
        return saveDpAndFlexUseCase(currentLineup, dp, flex, _listPlayersWithPosition)
            .onSuccess {
                recomputeAndPublish()
                _uiState.update { it.copy(dpFlexEvent = null) }
            }
            .onFailure { throwable ->
                if (throwable is NeedAssignBothPlayersException) {
                    Timber.w(throwable.message)
                    _uiState.update {
                        it.copy(infoMessageRes = R.string.error_need_assign_both_players_message)
                    }
                } else {
                    Timber.e(throwable)
                }
            }
    }

    fun onBattersChanged(batters: List<BatterState>) {
        viewModelScope.launch {
            updatePlayersWithBattersUseCase(_listPlayersWithPosition, batters)
                .onFailure { Timber.e(it) }
        }
    }

    fun onRosterEditLoaded(lineup: Lineup, roster: TeamRosterSummary) {
        _uiState.update {
            it.copy(
                rosterEditLineup = lineup,
                rosterEditSummary = roster,
                rosterEditOriginalSummary = roster,
                showRosterEditSheet = true
            )
        }
    }

    fun onRosterEditPlayerToggled(position: Int, checked: Boolean) {
        _uiState.update {
            it.copy(
                rosterEditSummary = toggleRosterPlayerUseCase(it.rosterEditSummary, position, checked)
            )
        }
    }

    fun onRosterEditNumberChanged(position: Int, number: Int) {
        _uiState.update {
            val lineupId = it.rosterEditLineup?.id ?: 0L
            it.copy(
                rosterEditSummary = overrideRosterPlayerNumberUseCase(
                    it.rosterEditSummary,
                    position,
                    number,
                    lineupId
                )
            )
        }
    }

    fun onRosterEditClearAll() {
        _uiState.update { it.copy(rosterEditSummary = clearRosterUseCase(it.rosterEditSummary)) }
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
