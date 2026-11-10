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
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.ObserveAllTournamentsWithLineups
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** Everything the Tournaments tab (and TeamDetails' own tournament/lineup counts) need. */
data class LineupListUiState(
    val isLoading: Boolean = true,
    val tournamentItems: ImmutableList<TournamentItem> = persistentListOf(),
    val mapsByTournamentId: ImmutableMap<Long, GeoLocation> = persistentMapOf(),
    val searchQuery: String = "",
    val teamType: TeamType = TeamType.UNKNOWN
)

class LineupListViewModel(
    private val getTeamUseCase: GetTeam,
    private val observeAllTournamentsWithLineups: ObserveAllTournamentsWithLineups,
    private val getTournamentMapLink: GetTournamentMapLink,
    private val deleteTournamentLineups: DeleteTournamentLineups
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupListUiState())
    val uiState: StateFlow<LineupListUiState> = _uiState.asStateFlow()

    private val filterFlow = MutableStateFlow("")
    private val mapAddressesByTournamentId = mutableMapOf<Long, String?>()

    init {
        // Observed, not loaded once: the list follows every lineup/tournament write, including
        // those made on another screen (creating a lineup, deleting it from the lineup screen).
        filterFlow
            .flatMapLatest { filter -> observeAllTournamentsWithLineups(filter) }
            .map { pairs ->
                pairs.map { TournamentItem(it.first, it.second.toImmutableList()) }
                    .toImmutableList()
            }
            .distinctUntilChanged()
            .catch { Timber.e(it) }
            .onEach { items ->
                _uiState.update { it.copy(isLoading = false, tournamentItems = items) }
                loadMaps(items)
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess { team ->
                    _uiState.update { it.copy(teamType = TeamType.getTypeById(team.type)) }
                }
                .onFailure { Timber.e(it) }
        }
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterFlow.value = query
    }

    suspend fun deleteTournament(tournament: Tournament): Result<Unit> {
        return deleteTournamentLineups(tournament)
    }
}
