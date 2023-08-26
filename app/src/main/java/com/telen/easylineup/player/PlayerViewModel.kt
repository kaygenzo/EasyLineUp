package com.telen.easylineup.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
    var strategies = mutableListOf<String>()

    var savedName: String? = null
    var savedShirtNumber: Int? = null
    var savedLicenseNumber: Long? = null
    var savedImage: String? = null
    var savedPositions: Int? = null
    var savedPitching: Int? = null
    var savedBatting: Int? = null
    var savedEmail: String? = null
    var savedPhoneNumber: String? = null
    var savedSex: Int? = null

    fun observePlayerName(): LiveData<String> {
        return _player.map { savedName ?: it.name }
    }

    fun observePlayerShirtNumber(): LiveData<Int> {
        return _player.map { savedShirtNumber ?: it.shirtNumber }
    }

    fun observePlayerLicenseNumber(): LiveData<Long> {
        return _player.map { savedLicenseNumber ?: it.licenseNumber }
    }

    fun observePlayerImage(): LiveData<String?> {
        return _player.map { savedImage ?: it.image }
    }

    fun observePlayerPosition(): LiveData<Int> {
        return _player.map { savedPositions ?: it.positions }
    }

    fun observePlayerPitchingSide(): LiveData<Int> {
        return _player.map { savedPitching ?: it.pitching }
    }

    fun observePlayerBattingSide(): LiveData<Int> {
        return _player.map { savedBatting ?: it.batting }
    }

    fun observePlayerEmail(): LiveData<String> {
        return _player.map { savedEmail ?: it.email ?: "" }
    }

    fun observePlayerPhoneNumber(): LiveData<String> {
        return _player.map { savedPhoneNumber ?: it.phone ?: "" }
    }

    fun observePlayerSex(): LiveData<Int> {
        return _player.map { savedSex ?: it.sex }
    }

    fun observeStrategy(): LiveData<TeamStrategy> {
        return _strategyLiveData
    }

    fun observeStrategies(context: Context): LiveData<List<String>> {
        return observeTeamType().map { teamType ->
            val teamType = TeamType.getTypeById(teamType)
            val names = teamType.getStrategiesDisplayName(context) ?: arrayOf()
            strategies.apply {
                clear()
                addAll(names)
                _strategyLiveData.postValue(teamType.getStrategies()[0])
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
        phone: String?,
        sex: Int
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
            phone,
            sex
        )
    }

    fun deletePlayer(): Completable {
        return domain.players().deletePlayer(playerID)
    }

    fun registerPlayerFormErrorResult() = domain.players().observeErrors()

    fun onStrategySelected(index: Int) {
        val teamType = TeamType.getTypeById(this.teamType)
        _strategyLiveData.postValue(teamType.getStrategies()[index])
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