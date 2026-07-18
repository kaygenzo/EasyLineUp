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
import com.telen.easylineup.utils.asSafeFlow
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TeamViewModel : ViewModel(), KoinComponent {
    private val getTeamUseCase: GetTeam by inject()
    private val deleteTeamUseCase: DeleteTeam by inject()
    private val observePlayers: ObservePlayers by inject()
    private val _team: MutableSharedFlow<Team> by lazy {
        MutableSharedFlow<Team>(replay = 1, extraBufferCapacity = 1).apply { getCurrentTeam() }
    }
    private val _playersFromDao by lazy {
        _team.flatMapLatest {
            observePlayers(it.id).asSafeFlow()
        }
    }
    private val _players: MutableSharedFlow<List<Player>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    // Emulates a MediatorLiveData with a shared Observer on 2 sources: passthrough merge,
    // not a combine - whichever source emits wins, re-triggering the sort downstream.
    private val _playersMerged: Flow<List<Player>> by lazy {
        merge(_playersFromDao, _players).onEach { playerList = it }
    }
    private val _displayType: MutableStateFlow<DisplayType> = MutableStateFlow(DisplayType.GRID)
    private val disposables = CompositeDisposable()
    private var playerSelectedId = 0L
    var team: Team? = null
    var sortType: SortType = SortType.ALPHA
        private set
    var displayType: DisplayType = DisplayType.GRID
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

    fun observeDisplayType(): Flow<DisplayType> = _displayType

    fun clear() {
        disposables.clear()
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

    fun deleteTeam(team: Team) = deleteTeamUseCase(team)

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
        val disposable = getTeamUseCase()
            .subscribe({
                team = it
                _team.tryEmit(it)
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
    }

    fun switchDisplayType() {
        when (this.displayType) {
            DisplayType.LIST -> this.displayType = DisplayType.GRID
            DisplayType.GRID -> this.displayType = DisplayType.LIST
        }
        _displayType.value = this.displayType
    }

    fun setSortType(sortType: SortType) {
        this.sortType = sortType
        _players.tryEmit(playerList)
    }

    private fun sortPlayers(listPlayers: List<Player>): List<Player> {
        return when (sortType) {
            SortType.ALPHA -> listPlayers.sortedBy { it.name }.toMutableList()
            SortType.NUMERIC -> listPlayers.sortedBy { it.shirtNumber }.toMutableList()
        }
    }

    enum class SortType {
        ALPHA, NUMERIC
    }

    enum class DisplayType {
        LIST, GRID
    }
}
