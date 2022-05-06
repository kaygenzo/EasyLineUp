package com.telen.easylineup.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TeamViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

    private var playerSelectedID = 0L
    var team: Team? = null

    private val _playersLiveData = MutableLiveData<List<Player>>()
    private val _teamLiveData = MutableLiveData<Team>()

    private val disposables = CompositeDisposable()

    fun observePlayers(): LiveData<List<Player>> {
        val disposable = domain.players().getPlayers()
                .subscribe({
                    _playersLiveData.postValue(it)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
        return _playersLiveData
    }

    fun clear() {
        disposables.clear()
    }

    fun observeTeam(): LiveData<Team> {
        val disposable = domain.teams().getTeam()
                .subscribe({
                    this.team = it
                    _teamLiveData.postValue(it)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
        return _teamLiveData
    }

    fun deleteTeam(team: Team): Completable {
        return domain.teams().deleteTeam(team)
    }

    fun getPlayerId() : Long {
        return playerSelectedID
    }

    fun setPlayerId(id: Long) {
        playerSelectedID = id
    }
}