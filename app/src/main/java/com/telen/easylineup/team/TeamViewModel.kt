/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.team

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSortType
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.ObserveCurrentTeam
import com.telen.easylineup.domain.usecases.ObservePlayers
import com.telen.easylineup.domain.usecases.SortPlayers
import com.telen.easylineup.domain.usecases.exceptions.NoTeamsLeftException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Everything any screen showing "the current team" needs - shared by Roster, PlayerDetails,
 * TeamDetails, Settings and Tournaments, each reading only the slice it renders.
 */
data class TeamUiState(
    val isLoading: Boolean = true,

    /** Reading the team or its players failed: the roster shows a retry state, not "empty". */
    val loadFailed: Boolean = false,
    val teamName: String = "",
    val teamType: TeamType = TeamType.UNKNOWN,
    val teamImageUri: Uri? = null,
    val teamCreatedAt: Long = 0L,
    val players: ImmutableList<Player> = persistentListOf(),
    val sortType: PlayerSortType = PlayerSortType.ALPHA
)

/** One-shot outcomes of a team deletion, for the Route to navigate or show a toast. */
sealed interface TeamEvent {
    data object TeamDeleted : TeamEvent

    /** The deleted team was the last one: the app has no team left to show. */
    data object LastTeamDeleted : TeamEvent
    data object TeamDeleteFailed : TeamEvent
}

class TeamViewModel(
    private val observeCurrentTeam: ObserveCurrentTeam,
    private val deleteTeamUseCase: DeleteTeam,
    private val observePlayersUseCase: ObservePlayers,
    private val sortPlayersUseCase: SortPlayers
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    private val _events = Channel<TeamEvent>(Channel.BUFFERED)
    val events: Flow<TeamEvent> = _events.receiveAsFlow()

    /** Bumped by [onRetryLoad] to start reading again after a failure ended the stream. */
    private val loadAttempt = MutableStateFlow(0)
    private var rawPlayers: List<Player> = emptyList()

    /** The current team, for the rare direct (non-reactive) read a delete/edit action needs. */
    var team: Team? = null
        private set

    init {
        // Followed rather than read once: this ViewModel outlives a team switch (the bottom
        // navigation keeps each tab's state), and must then show the new current team.
        loadAttempt
            .flatMapLatest {
                observeCurrentTeam().catch { e ->
                    Timber.e(e)
                    markLoadFailed()
                }
            }
            .onEach(::showTeam)
            .flatMapLatest { team ->
                if (team == null) {
                    flowOf(emptyList())
                } else {
                    observePlayersUseCase(team.id).catch { e ->
                        Timber.e(e)
                        markLoadFailed()
                    }
                }
            }
            .onEach { players ->
                rawPlayers = players
                applySort()
            }
            .launchIn(viewModelScope)
    }

    private fun showTeam(team: Team?) {
        this.team = team
        _uiState.update {
            it.copy(
                loadFailed = false,
                teamName = team?.name?.trim().orEmpty(),
                teamType = team?.let { current -> TeamType.getTypeById(current.type) }
                    ?: TeamType.UNKNOWN,
                teamImageUri = team?.image?.toUri(),
                teamCreatedAt = team?.createdAt ?: 0L
            )
        }
    }

    /** Reads the team again, and its players with it. */
    fun onRetryLoad() {
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        loadAttempt.update { it + 1 }
    }

    private fun markLoadFailed() {
        _uiState.update { it.copy(isLoading = false, loadFailed = true) }
    }

    fun setSortType(sortType: PlayerSortType) {
        _uiState.update { it.copy(sortType = sortType) }
        applySort()
    }

    private fun applySort() {
        val sorted = sortPlayersUseCase(rawPlayers, _uiState.value.sortType)
        _uiState.update { it.copy(isLoading = false, players = sorted.toImmutableList()) }
    }

    /** Deletes [team] in this ViewModel's scope, so leaving the screen can't cancel it. */
    fun onDeleteTeam(team: Team) {
        viewModelScope.launch {
            deleteTeamUseCase(team)
                .onSuccess { _events.send(TeamEvent.TeamDeleted) }
                .onFailure { throwable ->
                    // DeleteTeam still commits the delete when it reports NoTeamsLeftException:
                    // there was just no other team left to promote to main.
                    if (throwable is NoTeamsLeftException) {
                        _events.send(TeamEvent.LastTeamDeleted)
                    } else {
                        Timber.e(throwable)
                        _events.send(TeamEvent.TeamDeleteFailed)
                    }
                }
        }
    }
}
