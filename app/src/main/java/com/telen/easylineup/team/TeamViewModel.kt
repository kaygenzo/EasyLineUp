/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.team

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSortType
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayers
import com.telen.easylineup.domain.usecases.SortPlayers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Everything any screen showing "the current team" needs - shared by Roster, PlayerDetails,
 * TeamDetails, Settings and Tournaments, each reading only the slice it renders.
 */
data class TeamUiState(
    val isLoading: Boolean = true,
    val teamName: String = "",
    val teamType: TeamType = TeamType.UNKNOWN,
    val teamImageUri: Uri? = null,
    val teamCreatedAt: Long = 0L,
    val players: ImmutableList<Player> = persistentListOf(),
    val sortType: PlayerSortType = PlayerSortType.ALPHA
)

class TeamViewModel(
    private val getTeamUseCase: GetTeam,
    private val deleteTeamUseCase: DeleteTeam,
    private val observePlayersUseCase: ObservePlayers,
    private val sortPlayersUseCase: SortPlayers
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    private val _team: MutableSharedFlow<Team> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private var rawPlayers: List<Player> = emptyList()

    /** The current team, for the rare direct (non-reactive) read a delete/edit action needs. */
    var team: Team? = null
        private set

    init {
        loadTeam()

        _team.flatMapLatest { team ->
            observePlayersUseCase(team.id).catch { e -> Timber.e(e) }
        }
            .onEach { players ->
                rawPlayers = players
                applySort()
            }
            .launchIn(viewModelScope)
    }

    fun loadTeam() {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess { team ->
                    this@TeamViewModel.team = team
                    _uiState.update {
                        it.copy(
                            teamName = team.name.trim(),
                            teamType = TeamType.getTypeById(team.type),
                            teamImageUri = team.image?.let(Uri::parse),
                            teamCreatedAt = team.createdAt
                        )
                    }
                    _team.tryEmit(team)
                }
                .onFailure { Timber.e(it) }
        }
    }

    fun setSortType(sortType: PlayerSortType) {
        _uiState.update { it.copy(sortType = sortType) }
        applySort()
    }

    private fun applySort() {
        val sorted = sortPlayersUseCase(rawPlayers, _uiState.value.sortType)
        _uiState.update { it.copy(isLoading = false, players = sorted.toImmutableList()) }
    }

    suspend fun deleteTeam(team: Team): Result<Unit> = deleteTeamUseCase(team)
}
