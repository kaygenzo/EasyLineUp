/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayer
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** The handful of positions the roster edit/profile position pickers offer, one per mask group. */
val availablePositions: List<FieldPosition> = FieldPosition.values()
    .filter { it.isDefensePlayer() }
    .groupBy { it.mask }
    .map { it.value.first() }

/**
 * Everything a player's profile view and its edit form need - one ViewModel instance per
 * destination (Koin scopes it to the NavBackStackEntry), so [PlayerDetailsRoute] and
 * [PlayerEditRoute] never share state, only the same shape of it.
 */
data class PlayerUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val nameError: Boolean = false,
    val shirtNumberText: String = "",
    val licenseNumberText: String = "",
    val imageUri: Uri? = null,
    val sex: Sex = Sex.UNKNOWN,
    val battingLeft: Boolean = false,
    val battingRight: Boolean = false,
    val pitchingLeft: Boolean = false,
    val pitchingRight: Boolean = false,
    val selectedPositions: Set<FieldPosition> = emptySet(),
    val email: String = "",
    val emailError: Boolean = false,
    val phone: String = "",
    val phoneError: Boolean = false,
    val positionsData: Map<FieldPosition, Int> = emptyMap(),
    val teamType: TeamType = TeamType.getTypeById(0),
    val strategy: TeamStrategy = TeamStrategy.STANDARD
) {
    val shirtNumber: Int get() = shirtNumberText.toIntOrNull() ?: 0
    val licenseNumber: Long get() = licenseNumberText.toLongOrNull() ?: 0L
    val battingSide: PlayerSide?
        get() = PlayerSide.getSideByValue(sideFlag(battingLeft, battingRight))
    val pitchingSide: PlayerSide?
        get() = PlayerSide.getSideByValue(sideFlag(pitchingLeft, pitchingRight))
}

private fun sideFlag(left: Boolean, right: Boolean): Int {
    var flag = 0
    if (left) flag = flag or PlayerSide.LEFT.flag
    if (right) flag = flag or PlayerSide.RIGHT.flag
    return flag
}

class PlayerViewModel(
    private val getTeamUseCase: GetTeam,
    private val observePlayer: ObservePlayer,
    private val savePlayerUseCase: SavePlayer,
    private val deletePlayerUseCase: DeletePlayer,
    private val getPlayerPositionsSummaryUseCase: GetPositionsSummaryForPlayer
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /** `0` means "new player" - set once by the Route right after obtaining this instance. */
    var playerId: Long = 0
        set(value) {
            field = value
            loadPlayer(value)
        }

    init {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess { team ->
                    val resolvedType = TeamType.getTypeById(team.type)
                    _uiState.update {
                        it.copy(teamType = resolvedType, strategy = resolvedType.defaultStrategy)
                    }
                }
                .onFailure { Timber.e(it) }
        }
    }

    private fun loadPlayer(id: Long) {
        if (id <= 0) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        observePlayer(id)
            .catch { Timber.e(it) }
            .onEach { player ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = player.name,
                        shirtNumberText = player.shirtNumber.takeIf { n -> n > 0 }?.toString() ?: "",
                        licenseNumberText = player.licenseNumber.takeIf { n -> n > 0 }?.toString()
                            ?: "",
                        imageUri = player.image?.let(Uri::parse),
                        sex = Sex.getById(player.sex),
                        battingLeft = (player.batting and PlayerSide.LEFT.flag) != 0,
                        battingRight = (player.batting and PlayerSide.RIGHT.flag) != 0,
                        pitchingLeft = (player.pitching and PlayerSide.LEFT.flag) != 0,
                        pitchingRight = (player.pitching and PlayerSide.RIGHT.flag) != 0,
                        selectedPositions = availablePositions
                            .filter { position -> (player.positions and position.mask) != 0 }
                            .toSet(),
                        email = player.email ?: "",
                        phone = player.phone ?: ""
                    )
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            getPlayerPositionsSummaryUseCase(id)
                .onSuccess { summary -> _uiState.update { it.copy(positionsData = summary) } }
                .onFailure { Timber.e(it) }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = false) }
    }

    fun onShirtNumberChanged(text: String) {
        _uiState.update { it.copy(shirtNumberText = text.filter(Char::isDigit)) }
    }

    fun onLicenseNumberChanged(text: String) {
        _uiState.update { it.copy(licenseNumberText = text.filter(Char::isDigit)) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun onSexSelected(sex: Sex) {
        _uiState.update { it.copy(sex = sex) }
    }

    fun onBattingLeftToggled() {
        _uiState.update { it.copy(battingLeft = !it.battingLeft) }
    }

    fun onBattingRightToggled() {
        _uiState.update { it.copy(battingRight = !it.battingRight) }
    }

    fun onPitchingLeftToggled() {
        _uiState.update { it.copy(pitchingLeft = !it.pitchingLeft) }
    }

    fun onPitchingRightToggled() {
        _uiState.update { it.copy(pitchingRight = !it.pitchingRight) }
    }

    fun onPositionToggled(position: FieldPosition) {
        _uiState.update {
            val newSelection = it.selectedPositions.toMutableSet()
            if (position in newSelection) {
                newSelection.remove(position)
            } else if (newSelection.size < MAX_FAVORITE_POSITIONS) {
                newSelection.add(position)
            }
            it.copy(selectedPositions = newSelection)
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = false) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = false) }
    }

    fun onStrategySelected(strategy: TeamStrategy) {
        _uiState.update { it.copy(strategy = strategy) }
    }

    /** Returns whether the save actually went through - the Route navigates up only then. */
    suspend fun onSaveClick(): Boolean {
        val current = _uiState.value
        return savePlayerUseCase(
            playerId,
            current.name,
            current.shirtNumberText.toIntOrNull(),
            current.licenseNumberText.toLongOrNull(),
            current.imageUri?.toString(),
            current.selectedPositions.fold(0) { acc, position -> acc or position.mask },
            sideFlag(current.pitchingLeft, current.pitchingRight),
            sideFlag(current.battingLeft, current.battingRight),
            current.email,
            current.phone,
            current.sex.id
        ).fold(
            onSuccess = { true },
            onFailure = { throwable ->
                _uiState.update {
                    it.copy(
                        nameError = throwable is NameEmptyException,
                        emailError = throwable is InvalidEmailException,
                        phoneError = throwable is InvalidPhoneException
                    )
                }
                if (throwable !is NameEmptyException &&
                    throwable !is InvalidEmailException &&
                    throwable !is InvalidPhoneException
                ) {
                    Timber.e(throwable)
                }
                false
            }
        )
    }

    suspend fun deletePlayer(): Result<Unit> = deletePlayerUseCase(playerId)

    private companion object {
        const val MAX_FAVORITE_POSITIONS = 3
    }
}
