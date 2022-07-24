package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class PlayerViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

    private val disposables = CompositeDisposable()
    private val _teamTypeLiveData = MutableLiveData<Int>().apply { getTeamType() }
    private val _strategyLiveData = MutableLiveData<TeamStrategy>()
    private val _lineupsLiveData by lazy {
        MutableLiveData<Map<FieldPosition, Int>>().apply { getLineups() }
    }
    private val _player by lazy {
        playerID.takeIf { it > 0 }
            ?.let { domain.players().getPlayer(it) }
            ?: MutableLiveData()

    }

    var playerID: Long = 0
    var teamType: Int = 0
    var strategies = mutableListOf<TeamStrategy>()

    var savedName: String? = null
    var savedShirtNumber: Int? = null
    var savedLicenseNumber: Long? = null
    var savedImage: String? = null
    var savedPositions: Int? = null
    var savedPitching: Int? = null
    var savedBatting: Int? = null
    var savedEmail: String? = null
    var savedPhoneNumber: String? = null

    fun observePlayerName(): LiveData<String> {
        return Transformations.map(_player) { savedName ?: it.name }
    }

    fun observePlayerShirtNumber(): LiveData<Int> {
        return Transformations.map(_player) { savedShirtNumber ?: it.shirtNumber }
    }

    fun observePlayerLicenseNumber(): LiveData<Long> {
        return Transformations.map(_player) { savedLicenseNumber ?: it.licenseNumber }
    }

    fun observePlayerImage(): LiveData<String?> {
        return Transformations.map(_player) { savedImage ?: it.image }
    }

    fun observePlayerPosition(): LiveData<Int> {
        return Transformations.map(_player) { savedPositions ?: it.positions }
    }

    fun observePlayerPitchingSide(): LiveData<Int> {
        return Transformations.map(_player) { savedPitching ?: it.pitching }
    }

    fun observePlayerBattingSide(): LiveData<Int> {
        return Transformations.map(_player) { savedBatting ?: it.batting }
    }

    fun observePlayerEmail(): LiveData<String> {
        return Transformations.map(_player) { savedEmail ?: it.email }
    }

    fun observePlayerPhoneNumber(): LiveData<String> {
        return Transformations.map(_player) { savedPhoneNumber ?: it.phone }
    }

    fun observeStrategy(): LiveData<TeamStrategy> {
        return _strategyLiveData
    }

    fun observeStrategies(): LiveData<List<TeamStrategy>> {
        return Transformations.map(observeTeamType()) { teamType ->
            strategies.apply {
                clear()
                addAll(TeamType.getTypeById(teamType).getStrategies())
                _strategyLiveData.postValue(this[0])
            }
        }
    }

    fun observeTeamType(): LiveData<Int> {
        return _teamTypeLiveData
    }

    private fun getTeamType() {
        val disposable = domain.teams().getTeamType().subscribe({
            this.teamType = it
            _teamTypeLiveData.postValue(it)
        }, {
            Timber.e(it)
        })
        disposables.add(disposable)
    }

    fun observeLineups(): LiveData<Map<FieldPosition, Int>> {
        return _lineupsLiveData
    }

    fun clear() {
        disposables.clear()
    }

    fun savePlayer(
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long?,
        imageUri: Uri?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?
    ): Completable {
        return domain.players().savePlayer(
            playerID,
            name,
            shirtNumber,
            licenseNumber,
            imageUri,
            positions,
            pitching,
            batting,
            email,
            phone
        )
    }

    fun deletePlayer(): Completable {
        return domain.players().deletePlayer(playerID)
    }

    fun registerPlayerFormErrorResult() = domain.players().observeErrors()

    fun onStrategySelected(index: Int) {
        _strategyLiveData.postValue(strategies[index])
    }

    private fun getLineups() {
        val disposable = domain.players().getPlayerPositionsSummary(playerID)
            .subscribe({
                _lineupsLiveData.postValue(it)
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
    }
}