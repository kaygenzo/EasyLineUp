package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.Player
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

enum class FormErrorResult {
    INVALID_NAME,
    INVALID_LICENSE,
    INVALID_NUMBER,
    INVALID_ID
}

class PlayerViewModel: ViewModel(), KoinComponent {

    private val errorResult = MutableLiveData<FormErrorResult>()

    private val getTeamUseCase: GetTeam by inject()
    private val getPlayerUseCase: GetPlayer by inject()
    private val deletePlayerUseCase: DeletePlayer by inject()
    private val savePlayerUseCase: SavePlayer by inject()
    private val getPlayerPositionsSummaryUseCase: GetPositionsSummaryForPlayer by inject()

    private val disposables = CompositeDisposable()

    private val _teamTypeLiveData = MutableLiveData<Int>()
    private val _playerLiveData = MutableLiveData<Player>()
    private val _lineupsLiveData = MutableLiveData<Map<FieldPosition, Int>>()

    var playerID: Long? = 0

    fun observePlayer(): LiveData<Player> {
        val disposable = UseCaseHandler.execute(getPlayerUseCase, GetPlayer.RequestValues(playerID))
                .map { it.player }
                .subscribe({
                    _playerLiveData.postValue(it)
                }, {
                    if(it is NotExistingPlayer) {
                        errorResult.value = FormErrorResult.INVALID_ID
                    }
                    Timber.e(it)
                })
        disposables.add(disposable)
        return _playerLiveData
    }

    fun observeTeamType(): LiveData<Int> {
        val disposable = UseCaseHandler.execute(getTeamUseCase,GetTeam.RequestValues())
                .map { it.team.type }
                .subscribe({
                    _teamTypeLiveData.postValue(it)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
        return _teamTypeLiveData
    }

    fun observeLineups(): LiveData<Map<FieldPosition, Int>> {
        val disposable = UseCaseHandler.execute(getPlayerPositionsSummaryUseCase, GetPositionsSummaryForPlayer.RequestValues(playerID))
                .map { it.summary }
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
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMapCompletable {
                    val req = SavePlayer.RequestValues(playerID ?: 0, it.id, name, shirtNumber, licenseNumber, imageUri, positions)
                    UseCaseHandler.execute(savePlayerUseCase, req).ignoreElement()
                }
                .doOnError {
                    when (it) {
                        is NameEmptyException -> errorResult.value = FormErrorResult.INVALID_NAME
                        is ShirtNumberEmptyException -> errorResult.value = FormErrorResult.INVALID_NUMBER
                        is LicenseNumberEmptyException -> errorResult.value = FormErrorResult.INVALID_LICENSE
                    }
                }
    }

    fun deletePlayer(): Completable {
        return UseCaseHandler.execute(getPlayerUseCase, GetPlayer.RequestValues(playerID)).map { it.player }
                .flatMapCompletable { player -> UseCaseHandler.execute(deletePlayerUseCase, DeletePlayer.RequestValues(player)).ignoreElement() }
    }

    fun registerFormErrorResult(): LiveData<FormErrorResult> {
        return errorResult
    }
}