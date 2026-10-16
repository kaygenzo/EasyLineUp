/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.team

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayers
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class TeamViewModel(
    private val getTeamUseCase: GetTeam,
    private val deleteTeamUseCase: DeleteTeam,
    private val observePlayers: ObservePlayers
) : ViewModel() {
    private val _team: MutableSharedFlow<Team> by lazy {
        MutableSharedFlow<Team>(replay = 1, extraBufferCapacity = 1).apply { getCurrentTeam() }
    }
    private val _playersFromDao by lazy {
        _team.flatMapLatest {
            observePlayers(it.id).catch { e -> Timber.e(e) }
        }
    }
    private val _players: MutableSharedFlow<List<Player>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    // Emulates a MediatorLiveData with a shared Observer on 2 sources: passthrough merge,
    // not a combine - whichever source emits wins, re-triggering the sort downstream.
    private val _playersMerged: Flow<List<Player>> by lazy {
        merge(_playersFromDao, _players).onEach { playerList = it }
    }
    private var playerSelectedId = 0L
    var team: Team? = null
    var sortType: SortType = SortType.ALPHA
        private set
    private var playerList: List<Player> = listOf()

    init {
        // Force _team/_playersFromDao/_playersMerged lazy init eagerly, matching the
        // former MediatorLiveData.addSource(...) wiring that ran unconditionally at construction.
        _playersMerged
    }

    fun observePlayers(): Flow<List<Player>> = _playersMerged.map {
        sortPlayers(it)
    }

    fun clear() {
    }

    fun observeCurrentTeamName(): Flow<String> {
        return _team.map {
            it.name.trim()
        }
    }

    fun observeCurrentTeamType(): Flow<TeamType> {
        return _team.map {
            TeamType.getTypeById(it.type)
        }
    }

    fun observeCurrentTeamImage(): Flow<Uri?> {
        return _team.map {
            it.image.takeIf { it != null }?.let { Uri.parse(it) }
        }
    }

    fun observeCurrentTeamCreatedAt(): Flow<Long> {
        return _team.map { it.createdAt }
    }

    suspend fun deleteTeam(team: Team): Result<Unit> = deleteTeamUseCase(team)

    fun getPlayerId(): Long {
        return playerSelectedId
    }

    fun setPlayerId(id: Long) {
        playerSelectedId = id
    }

    fun loadTeam() {
        getCurrentTeam()
    }

    private fun getCurrentTeam() {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess {
                    team = it
                    _team.tryEmit(it)
                }
                .onFailure { Timber.e(it) }
        }
    }

    fun setSortType(sortType: SortType) {
        this.sortType = sortType
        _players.tryEmit(playerList)
    }

    private fun sortPlayers(listPlayers: List<Player>): List<Player> {
        return when (sortType) {
            SortType.ALPHA -> listPlayers.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) {
                it.name
            }).toMutableList()
            SortType.NUMERIC -> listPlayers.sortedBy { it.shirtNumber }.toMutableList()
        }
    }

    enum class SortType {
        ALPHA, NUMERIC
    }
}
