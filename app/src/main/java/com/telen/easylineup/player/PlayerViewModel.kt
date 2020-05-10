package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.utils.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

sealed class Event
object SavePlayerSuccess: Event()
object SavePlayerFailure: Event()
object DeletePlayerSuccess: Event()
data class DeletePlayerFailure(val message: String?): Event()

class PlayerViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val disposables = CompositeDisposable()

    private val _teamTypeLiveData = MutableLiveData<Int>()
    private val _playerLiveData = MutableLiveData<Player>()
    private val _lineupsLiveData = MutableLiveData<Map<FieldPosition, Int>>()
    private val _event = SingleLiveEvent<Event>()

    var playerID: Long? = 0

    fun observePlayer(): LiveData<Player> {
        val disposable = domain.getPlayer(playerID)
                .subscribe({
                    _playerLiveData.postValue(it)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
        return _playerLiveData
    }

    fun observeTeamType(): LiveData<Int> {
        val disposable = domain.getTeamType()
                .subscribe({
                    _teamTypeLiveData.postValue(it)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
        return _teamTypeLiveData
    }

    fun observeLineups(): LiveData<Map<FieldPosition, Int>> {
        val disposable = domain.getPlayerPositionsSummary(playerID)
                .subscribe({
                    _lineupsLiveData.postValue(it)
                }, {
                    Timber.e(it)
                })

        disposables.add(disposable)
        return _lineupsLiveData
    }

    fun clear() {
        disposables.clear()
    }

    fun savePlayer(name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int) {
        val disposable = domain.savePlayer(playerID, name, shirtNumber, licenseNumber, imageUri, positions)
                .subscribe({
                    _event.postValue(SavePlayerSuccess)
                }, {
                    Timber.e(it)
                    _event.postValue(SavePlayerFailure)
                })
        disposables.add(disposable)
    }

    fun deletePlayer() {
        val disposable = domain.deletePlayer(playerID)
                .subscribe({
                    _event.postValue(DeletePlayerSuccess)
                }, {
                    _event.postValue(DeletePlayerFailure(it.message))
                })
        disposables.add(disposable)
    }

    fun registerEvent(): LiveData<Event> {
        return _event
    }

    fun registerFormErrorResult(): LiveData<DomainErrors> {
        return domain.observeErrors()
    }
}