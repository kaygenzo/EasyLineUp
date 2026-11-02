/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** One-shot, non-render actions - everything else lives in [LineupCreationUiState]. */
sealed class LineupCreationEvent
data class LineupSaved(val lineupId: Long) : LineupCreationEvent()

/** Everything [LineupCreationScreen] and its sheets need to render. */
data class LineupCreationUiState(
    val isLoading: Boolean = true,
    val teamType: TeamType = TeamType.UNKNOWN,
    val name: String = "",
    val nameError: Boolean = false,
    val tournaments: ImmutableList<Tournament> = persistentListOf(),
    val selectedTournament: Tournament? = null,
    val tournamentError: Boolean = false,
    val eventTimeInMillis: Long = System.currentTimeMillis(),
    val roster: TeamRosterSummary = TeamRosterSummary(Constants.STATUS_ALL, emptyList()),
    val strategy: TeamStrategy = TeamStrategy.STANDARD,
    val extraHitters: Int = 0,
    val showTournamentSheet: Boolean = false,
    val showRosterSheet: Boolean = false,
    val showDiscardSheet: Boolean = false,
    val showNewTournamentSheet: Boolean = false,
    val newTournamentName: String = "",
    val newTournamentStartTime: Long = System.currentTimeMillis(),
    val newTournamentEndTime: Long = System.currentTimeMillis(),
    val newTournamentAddress: String = ""
)

class LineupCreationViewModel(
    private val observeTournaments: ObserveTournaments,
    private val getTeam: GetTeam,
    private val getRoster: GetRoster,
    private val createLineup: CreateLineup,
    private val saveTournament: SaveTournament,
    private val toggleRosterPlayerUseCase: ToggleRosterPlayer,
    private val clearRosterUseCase: ClearRoster,
    private val resolveLineupEventTimeUseCase: ResolveLineupEventTime
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupCreationUiState())
    val uiState: StateFlow<LineupCreationUiState> = _uiState.asStateFlow()

    private val _events = Channel<LineupCreationEvent>(Channel.BUFFERED)
    val events: Flow<LineupCreationEvent> = _events.receiveAsFlow()

    init {
        observeTournaments().catch { Timber.e(it) }
            .onEach { tournaments ->
                _uiState.update { it.copy(tournaments = tournaments.toImmutableList()) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            getTeam()
                .onSuccess { team ->
                    val resolvedType = TeamType.getTypeById(team.type)
                    // The strategy picker is only shown for team types with more than one valid
                    // strategy (getStrategiesDisplayName() returns null otherwise, e.g.
                    // BASEBALL_5) - for those, this default is never offered to the user to
                    // confirm or change, so it must already be correct for the team's type.
                    _uiState.update {
                        it.copy(teamType = resolvedType, strategy = resolvedType.defaultStrategy)
                    }
                }
                .onFailure { Timber.e(it) }
        }

        viewModelScope.launch {
            getRoster()
                .onSuccess { roster ->
                    _uiState.update { it.copy(isLoading = false, roster = roster) }
                }
                .onFailure {
                    Timber.e(it)
                    _uiState.update { state -> state.copy(isLoading = false) }
                }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = false) }
    }

    fun onTournamentSheetOpen() {
        _uiState.update { it.copy(showTournamentSheet = true) }
    }

    fun onTournamentSheetDismiss() {
        _uiState.update { it.copy(showTournamentSheet = false) }
    }

    fun onTournamentSelected(tournament: Tournament) {
        _uiState.update {
            it.copy(
                selectedTournament = tournament,
                tournamentError = false,
                eventTimeInMillis = resolveLineupEventTimeUseCase(tournament, it.eventTimeInMillis),
                showTournamentSheet = false
            )
        }
    }

    fun onEventTimeSelected(millis: Long) {
        _uiState.update { it.copy(eventTimeInMillis = millis) }
    }

    fun onRosterSheetOpen() {
        _uiState.update { it.copy(showRosterSheet = true) }
    }

    fun onRosterSheetDismiss() {
        _uiState.update { it.copy(showRosterSheet = false) }
    }

    fun onRosterPlayerToggled(position: Int, checked: Boolean) {
        _uiState.update {
            it.copy(roster = toggleRosterPlayerUseCase(it.roster, position, checked))
        }
    }

    fun onRosterClearAll() {
        _uiState.update { it.copy(roster = clearRosterUseCase(it.roster)) }
    }

    fun onStrategySelected(strategy: TeamStrategy) {
        _uiState.update { it.copy(strategy = strategy) }
    }

    fun onExtraHittersSelected(count: Int) {
        _uiState.update { it.copy(extraHitters = count) }
    }

    fun onNewTournamentSheetOpen() {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                showTournamentSheet = false,
                showNewTournamentSheet = true,
                newTournamentName = "",
                newTournamentStartTime = now,
                newTournamentEndTime = now,
                newTournamentAddress = ""
            )
        }
    }

    fun onNewTournamentSheetDismiss() {
        _uiState.update { it.copy(showNewTournamentSheet = false) }
    }

    fun onNewTournamentNameChanged(name: String) {
        _uiState.update { it.copy(newTournamentName = name) }
    }

    fun onNewTournamentAddressChanged(address: String) {
        _uiState.update { it.copy(newTournamentAddress = address) }
    }

    fun onNewTournamentStartTimeSelected(millis: Long) {
        _uiState.update { it.copy(newTournamentStartTime = millis) }
    }

    fun onNewTournamentEndTimeSelected(millis: Long) {
        _uiState.update { it.copy(newTournamentEndTime = millis) }
    }

    fun onAddNewTournament() {
        val current = _uiState.value
        val tournament = Tournament(
            name = current.newTournamentName,
            createdAt = System.currentTimeMillis(),
            startTime = current.newTournamentStartTime,
            endTime = current.newTournamentEndTime,
            address = current.newTournamentAddress.takeIf { it.isNotBlank() }
        )
        viewModelScope.launch {
            saveTournament(tournament)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            selectedTournament = tournament,
                            tournamentError = false,
                            eventTimeInMillis = resolveLineupEventTimeUseCase(
                                tournament,
                                it.eventTimeInMillis
                            ),
                            showNewTournamentSheet = false
                        )
                    }
                }
                .onFailure { Timber.e(it) }
        }
    }

    /** Returns whether it is safe to leave immediately (no unsaved changes to discard). */
    fun onCancelRequested(): Boolean {
        val current = _uiState.value
        return if (current.name.isNotBlank() || current.selectedTournament != null) {
            _uiState.update { it.copy(showDiscardSheet = true) }
            false
        } else {
            true
        }
    }

    fun onDiscardDismiss() {
        _uiState.update { it.copy(showDiscardSheet = false) }
    }

    fun onSaveClick() {
        val current = _uiState.value
        val lineup = Lineup(
            name = current.name,
            tournamentId = current.selectedTournament?.id ?: 0,
            strategy = current.strategy.id,
            extraHitters = current.extraHitters,
            eventTimeInMillis = current.eventTimeInMillis
        )
        viewModelScope.launch {
            createLineup(lineup, current.roster.players)
                .onSuccess { _events.send(LineupSaved(it.id)) }
                .onFailure {
                    when (it) {
                        is LineupNameEmptyException ->
                            _uiState.update { state -> state.copy(nameError = true) }
                        is TournamentNameEmptyException ->
                            _uiState.update { state -> state.copy(tournamentError = true) }
                        else -> Timber.e(it)
                    }
                }
        }
    }
}
