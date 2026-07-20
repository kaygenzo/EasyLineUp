/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayer
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.utils.getStrategiesDisplayName
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class PlayerViewModel : ViewModel(), KoinComponent {
    private val getTeamUseCase: GetTeam by inject()
    private val observePlayer: ObservePlayer by inject()
    private val savePlayerUseCase: SavePlayer by inject()
    private val deletePlayerUseCase: DeletePlayer by inject()
    private val getPlayerPositionsSummaryUseCase: GetPositionsSummaryForPlayer by inject()
    private val errors: Subject<DomainErrors.Players> = PublishSubject.create()
    private val _teamTypeFlow: MutableSharedFlow<Int> =
        MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 1).apply {
            getTeamType()
        }
    private val _strategyFlow: MutableSharedFlow<TeamStrategy> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val _lineupsFlow: MutableSharedFlow<Map<FieldPosition, Int>> by lazy {
        MutableSharedFlow<Map<FieldPosition, Int>>(replay = 1, extraBufferCapacity = 1)
            .apply { getLineups() }
    }
    private val _player: Flow<Player> by lazy {
        playerId.takeIf { it > 0 }
            ?.let { observePlayer(it).catch { Timber.e(it) } }
            ?: flowOf(Player(teamId = 0, name = "", shirtNumber = 0, licenseNumber = 0))
    }
    var playerId: Long = 0
    var teamType: Int = 0
    var strategies: MutableList<String> = mutableListOf()
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

    fun observePlayerName(): Flow<String> {
        return _player.map { savedName ?: it.name }
    }

    fun observePlayerShirtNumber(): Flow<Int> {
        return _player.map { savedShirtNumber ?: it.shirtNumber }
    }

    fun observePlayerLicenseNumber(): Flow<Long> {
        return _player.map { savedLicenseNumber ?: it.licenseNumber }
    }

    fun observePlayerImage(): Flow<String?> {
        return _player.map { savedImage ?: it.image }
    }

    fun observePlayerPosition(): Flow<Int> {
        return _player.map { savedPositions ?: it.positions }
    }

    fun observePlayerPitchingSide(): Flow<Int> {
        return _player.map { savedPitching ?: it.pitching }
    }

    fun observePlayerBattingSide(): Flow<Int> {
        return _player.map { savedBatting ?: it.batting }
    }

    fun observePlayerEmail(): Flow<String> {
        return _player.map { savedEmail ?: it.email ?: "" }
    }

    fun observePlayerPhoneNumber(): Flow<String> {
        return _player.map { savedPhoneNumber ?: it.phone ?: "" }
    }

    fun observePlayerSex(): Flow<Int> {
        return _player.map { savedSex ?: it.sex }
    }

    fun observeStrategy(): Flow<TeamStrategy> {
        return _strategyFlow
    }

    fun observeStrategies(context: Context): Flow<List<String>> {
        return observeTeamType().map { teamType ->
            val teamType = TeamType.getTypeById(teamType)
            val names = teamType.getStrategiesDisplayName(context) ?: arrayOf()
            strategies.apply {
                clear()
                addAll(names)
                _strategyFlow.tryEmit(teamType.getStrategies()[0])
            }
        }
    }

    fun observeTeamType(): Flow<Int> {
        return _teamTypeFlow
    }

    private fun getTeamType() {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess {
                    this@PlayerViewModel.teamType = it.type
                    _teamTypeFlow.tryEmit(it.type)
                }
                .onFailure { Timber.e(it) }
        }
    }

    fun observeLineups(): Flow<Map<FieldPosition, Int>> {
        return _lineupsFlow
    }

    fun clear() {
    }

    suspend fun savePlayer(
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long?,
        image: String?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?,
        sex: Int
    ): Result<Unit> {
        return savePlayerUseCase(
            playerId,
            name,
            shirtNumber,
            licenseNumber,
            image,
            positions,
            pitching,
            batting,
            email,
            phone,
            sex
        ).onFailure {
            when (it) {
                is NameEmptyException ->
                    errors.onNext(DomainErrors.Players.INVALID_PLAYER_NAME)
                is InvalidEmailException ->
                    errors.onNext(DomainErrors.Players.INVALID_EMAIL_FORMAT)
                is InvalidPhoneException ->
                    errors.onNext(DomainErrors.Players.INVALID_PHONE_NUMBER_FORMAT)
            }
        }
    }

    suspend fun deletePlayer(): Result<Unit> {
        return deletePlayerUseCase(playerId)
    }

    fun registerPlayerFormErrorResult(): Subject<DomainErrors.Players> = errors

    fun onStrategySelected(index: Int) {
        val teamType = TeamType.getTypeById(this.teamType)
        _strategyFlow.tryEmit(teamType.getStrategies()[index])
    }

    private fun getLineups() {
        viewModelScope.launch {
            getPlayerPositionsSummaryUseCase(playerId)
                .onSuccess { _lineupsFlow.tryEmit(it) }
                .onFailure { Timber.e(it) }
        }
    }
}
