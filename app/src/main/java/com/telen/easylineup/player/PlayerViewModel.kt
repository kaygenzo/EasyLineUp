package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class PlayerViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val disposables = CompositeDisposable()

    private val _teamTypeLiveData = MutableLiveData<Int>()
    private val _playerLiveData = MutableLiveData<Player>()
    private val _lineupsLiveData = MutableLiveData<Map<FieldPosition, Int>>()

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

    fun savePlayer(name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int): Completable {
        return domain.savePlayer(playerID, name, shirtNumber, licenseNumber, imageUri, positions)
    }

    fun deletePlayer(): Completable {
        return domain.deletePlayer(playerID)
    }

    fun registerFormErrorResult(): LiveData<DomainErrors> {
        return domain.observeErrors()
    }
}