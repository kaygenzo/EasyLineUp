package com.telen.easylineup.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.DeleteTeam
import com.telen.easylineup.domain.GetPlayers
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TeamViewModel: ViewModel(), KoinComponent {

    private var playerSelectedID = 0L
    var team: Team? = null

    private val getTeamUseCase: GetTeam by inject()
    private val getPlayersUseCase: GetPlayers by inject()
    private val deleteTeamUseCase: DeleteTeam by inject()

    private val _playersLiveData = MutableLiveData<List<Player>>()
    private val _teamLiveData = MutableLiveData<Team>()

    private val disposables = CompositeDisposable()

    fun observePlayers(): LiveData<List<Player>> {
        val disposable = UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team -> UseCaseHandler.execute(getPlayersUseCase, GetPlayers.RequestValues(team.id)).map { it.players } }
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
        val disposable = UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .map { it.team }
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
        return UseCaseHandler.execute(deleteTeamUseCase, DeleteTeam.RequestValues(team)).ignoreElement()
    }

    fun getPlayerId() : Long {
        return playerSelectedID
    }

    fun setPlayerId(id: Long) {
        playerSelectedID = id
    }
}