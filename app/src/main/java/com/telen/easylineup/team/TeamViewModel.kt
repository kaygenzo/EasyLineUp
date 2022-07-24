package com.telen.easylineup.team

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TeamViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    private val _team: MutableLiveData<Team> by lazy {
        MutableLiveData<Team>().apply { getCurrentTeam() }
    }
    private val _players: LiveData<List<Player>> by lazy {
        Transformations.switchMap(_team) {
            domain.players().observePlayers(it.id)
        }
    }
    private val disposables = CompositeDisposable()
    private var playerSelectedID = 0L
    var team: Team? = null

    fun observePlayers(): LiveData<List<Player>> {
        return _players
    }

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

    fun deleteTeam(team: Team): Completable {
        return domain.teams().deleteTeam(team)
    }

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
}