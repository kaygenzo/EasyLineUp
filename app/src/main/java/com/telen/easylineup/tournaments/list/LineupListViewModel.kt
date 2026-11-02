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
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineups
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
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
    private val getAllTournamentsWithLineups: GetAllTournamentsWithLineups,
    private val getTournamentMapLink: GetTournamentMapLink,
    private val deleteTournamentLineups: DeleteTournamentLineups
) : ViewModel() {
    private val _uiState = MutableStateFlow(LineupListUiState())
    val uiState: StateFlow<LineupListUiState> = _uiState.asStateFlow()

    private val filterFlow = MutableStateFlow("")
    private val reloadTrigger = MutableStateFlow(0)

    init {
        combine(filterFlow, reloadTrigger) { filter, _ -> filter }
            .flatMapLatest { filter ->
                flow {
                    getAllTournamentsWithLineups(filter)
                        .onSuccess { pairs ->
                            val items = pairs.map {
                                TournamentItem(it.first, it.second.toImmutableList())
                            }
                            emit(items.toImmutableList())
                        }
                        .onFailure { Timber.e(it) }
                }
            }
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
        val items = tournamentItems.filter { it.tournament.address != null }
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

    fun reloadList() {
        reloadTrigger.value++
    }

    suspend fun deleteTournament(tournament: Tournament): Result<Unit> {
        return deleteTournamentLineups(tournament).onSuccess { reloadTrigger.value++ }
    }
}
