/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.tournaments.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.ObserveAllTournamentsWithLineups
import com.telen.easylineup.domain.usecases.ObserveCurrentTeam
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** Everything the Tournaments tab (and TeamDetails' own tournament/lineup counts) need. */
data class LineupListUiState(
    val isLoading: Boolean = true,

    /** Reading the tournaments failed: the Route shows a retry state, not "no tournaments". */
    val loadFailed: Boolean = false,
    val tournamentItems: ImmutableList<TournamentItem> = persistentListOf(),
    val mapsByTournamentId: ImmutableMap<Long, GeoLocation> = persistentMapOf(),
    val searchQuery: String = "",
    val teamType: TeamType = TeamType.UNKNOWN
)

/** One-shot outcome of a tournament deletion that failed, for the Route's toast. */
/** A tournament's lineups couldn't be deleted; the cause only goes to the logs. */
data object TournamentDeleteFailed

class LineupListViewModel(
    private val observeCurrentTeam: ObserveCurrentTeam,
    private val observeAllTournamentsWithLineups: ObserveAllTournamentsWithLineups,
    private val getTournamentMapLink: GetTournamentMapLink,
    private val deleteTournamentLineups: DeleteTournamentLineups
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupListUiState())
    val uiState: StateFlow<LineupListUiState> = _uiState.asStateFlow()

    private val _events = Channel<TournamentDeleteFailed>(Channel.BUFFERED)
    val events: Flow<TournamentDeleteFailed> = _events.receiveAsFlow()

    private val filterFlow = MutableStateFlow("")

    /** Bumped by [onRetryLoad] to start reading again after a failure ended the stream. */
    private val loadAttempt = MutableStateFlow(0)
    private val mapAddressesByTournamentId = mutableMapOf<Long, String?>()

    init {
        // Observed, not loaded once: the list follows every lineup/tournament write, including
        // those made on another screen (creating a lineup, deleting it from the lineup screen).
        combine(filterFlow, loadAttempt) { filter, _ -> filter }
            .flatMapLatest { filter ->
                observeAllTournamentsWithLineups(filter)
                    .map { pairs ->
                        pairs.map { TournamentItem(it.first, it.second.toImmutableList()) }
                            .toImmutableList()
                    }
                    .distinctUntilChanged()
                    .onEach { items ->
                        _uiState.update {
                            it.copy(isLoading = false, loadFailed = false, tournamentItems = items)
                        }
                        loadMaps(items)
                    }
                    .catch {
                        Timber.e(it)
                        _uiState.update { state ->
                            state.copy(isLoading = false, loadFailed = true)
                        }
                    }
            }
            .launchIn(viewModelScope)

        // Followed like the list itself: this tab's ViewModel outlives a team switch.
        observeCurrentTeam()
            .filterNotNull()
            .onEach { team ->
                _uiState.update { it.copy(teamType = TeamType.getTypeById(team.type)) }
            }
            .catch { Timber.e(it) }
            .launchIn(viewModelScope)
    }

    private fun loadMaps(tournamentItems: List<TournamentItem>) {
        // The list now re-emits on every lineup write: only look up an address not looked up
        // yet (or changed since), rather than every tournament's map on each emission.
        val items = tournamentItems.filter {
            val address = it.tournament.address
            address != null && mapAddressesByTournamentId[it.tournament.id] != address
        }
        items.forEach { mapAddressesByTournamentId[it.tournament.id] = it.tournament.address }
        viewModelScope.launch {
            items.forEach { item ->
                getTournamentMapLink(item.tournament)
                    .onSuccess { location ->
                        val id = item.tournament.id
                        _uiState.update {
                            val updated = it.mapsByTournamentId + (id to location)
                            it.copy(mapsByTournamentId = updated.toImmutableMap())
                        }
                    }
            }
        }
    }

    fun onRetryLoad() {
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        loadAttempt.update { it + 1 }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterFlow.value = query
    }

    /**
     * Deletes [tournament]'s lineups in this ViewModel's scope, so leaving the screen can't
     * cancel it halfway. The list itself follows the database and needs no success event.
     */
    fun onDeleteTournament(tournament: Tournament) {
        viewModelScope.launch {
            deleteTournamentLineups(tournament).onFailure { throwable ->
                Timber.e(throwable)
                _events.send(TournamentDeleteFailed)
            }
        }
    }
}
