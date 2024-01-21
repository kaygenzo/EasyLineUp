/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TeamViewModel : ViewModel(), KoinComponent {
    private val domain: ApplicationInteractor by inject()
    private val _team: MutableLiveData<Team> by lazy {
        MutableLiveData<Team>().apply { getCurrentTeam() }
    }
    private val _playersFromDao by lazy {
        _team.switchMap {
            domain.players().observePlayers(it.id)
        }
    }
    private val _playersMediator: MediatorLiveData<List<Player>> = MediatorLiveData()
    private val _players: MutableLiveData<List<Player>> = MutableLiveData()
    private val _displayType: MutableLiveData<DisplayType> = MutableLiveData(DisplayType.GRID)
    private val disposables = CompositeDisposable()
    private var playerSelectedId = 0L
    var team: Team? = null
    var sortType: SortType = SortType.ALPHA
        private set
    var displayType: DisplayType = DisplayType.GRID
        private set
    private var playerList: List<Player> = listOf()

    init {
        val observer: Observer<List<Player>> = Observer {
            playerList = it
            _playersMediator.postValue(playerList)
        }
        _playersMediator.addSource(_playersFromDao, observer)
        _playersMediator.addSource(_players, observer)
    }

    fun observePlayers(): LiveData<List<Player>> = _playersMediator.map {
        sortPlayers(it)
    }

    fun observeDisplayType(): LiveData<DisplayType> = _displayType

    fun clear() {
        disposables.clear()
    }

    fun observeCurrentTeamName(): LiveData<String> {
        return _team.map {
            it.name.trim()
        }
    }

    fun observeCurrentTeamType(): LiveData<TeamType> {
        return _team.map {
            TeamType.getTypeById(it.type)
        }
    }

    fun observeCurrentTeamImage(): LiveData<Uri?> {
        return _team.map {
            it.image.takeIf { it != null }?.let { Uri.parse(it) }
        }
    }

    fun deleteTeam(team: Team) = domain.teams().deleteTeam(team)

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
        val disposable = domain.teams().getTeam()
            .subscribe({
                team = it
                _team.postValue(it)
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
        _displayType.postValue(this.displayType)
    }

    fun setSortType(sortType: SortType) {
        this.sortType = sortType
        _players.postValue(playerList)
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
