package com.telen.easylineup.team

import android.net.Uri
import androidx.lifecycle.*
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TeamViewModel : ViewModel(), KoinComponent {

    enum class SortType {
        ALPHA, NUMERIC
    }

    enum class DisplayType {
        LIST, GRID
    }

    private val domain: ApplicationInteractor by inject()
    private val _team: MutableLiveData<Team> by lazy {
        MutableLiveData<Team>().apply { getCurrentTeam() }
    }
    private val _playersFromDao by lazy {
        Transformations.switchMap(_team) {
            domain.players().observePlayers(it.id)
        }
    }
    private val _playersMediator = MediatorLiveData<List<Player>>()
    private val _players = MutableLiveData<List<Player>>()
    private val _displayType = MutableLiveData(DisplayType.GRID)

    private val disposables = CompositeDisposable()
    private var playerSelectedID = 0L
    var team: Team? = null
    var sortType: SortType = SortType.ALPHA
        private set
    var displayType: DisplayType = DisplayType.GRID
        private set
    private var playerList = listOf<Player>()

    init {
        val observer = Observer<List<Player>> {
            playerList = it
            _playersMediator.postValue(playerList)
        }
        _playersMediator.addSource(_playersFromDao, observer)
        _playersMediator.addSource(_players, observer)
    }

    fun observePlayers(): LiveData<List<Player>> = Transformations.map(_playersMediator) {
        sortPlayers(it)
    }

    fun observeDisplayType(): LiveData<DisplayType> = _displayType

    fun clear() {
        disposables.clear()
    }

    fun observeCurrentTeamName(): LiveData<String> {
        return Transformations.map(_team) {
            it.name.trim()
        }
    }

    fun observeCurrentTeamType(): LiveData<TeamType> {
        return Transformations.map(_team) {
            TeamType.getTypeById(it.type)
        }
    }

    fun observeCurrentTeamImage(): LiveData<Uri?> {
        return Transformations.map(_team) {
            it.image.takeIf { it != null }?.let { Uri.parse(it) }
        }
    }

    fun deleteTeam(team: Team) = domain.teams().deleteTeam(team)

    fun getPlayerId(): Long {
        return playerSelectedID
    }

    fun setPlayerId(id: Long) {
        playerSelectedID = id
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
            DisplayType.LIST -> {
                this.displayType = DisplayType.GRID
            }
            DisplayType.GRID -> {
                this.displayType = DisplayType.LIST
            }
        }
        _displayType.postValue(this.displayType)
    }

    fun setSortType(sortType: SortType) {
        this.sortType = sortType
        _players.postValue(playerList)
    }

    private fun sortPlayers(listPlayers: List<Player>): List<Player> {
        return when (sortType) {
            SortType.ALPHA -> {
                listPlayers.sortedBy { it.name }.toMutableList()
            }
            SortType.NUMERIC -> {
                listPlayers.sortedBy { it.shirtNumber }.toMutableList()
            }
        }
    }
}