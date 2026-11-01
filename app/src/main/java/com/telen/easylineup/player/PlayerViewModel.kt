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
import com.telen.easylineup.domain.model.matches
import com.telen.easylineup.domain.model.toPositionsMask
import com.telen.easylineup.domain.usecases.DecodePlayerSideFlag
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.EncodePlayerSideFlag
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayer
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.ToggleFavoritePosition
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
    val strategy: TeamStrategy = TeamStrategy.STANDARD,
    val battingSide: PlayerSide? = null,
    val pitchingSide: PlayerSide? = null
) {
    val shirtNumber: Int get() = shirtNumberText.toIntOrNull() ?: 0
    val licenseNumber: Long get() = licenseNumberText.toLongOrNull() ?: 0L
}

class PlayerViewModel(
    private val getTeamUseCase: GetTeam,
    private val observePlayer: ObservePlayer,
    private val savePlayerUseCase: SavePlayer,
    private val deletePlayerUseCase: DeletePlayer,
    private val getPlayerPositionsSummaryUseCase: GetPositionsSummaryForPlayer,
    private val encodePlayerSideFlagUseCase: EncodePlayerSideFlag,
    private val decodePlayerSideFlagUseCase: DecodePlayerSideFlag,
    private val toggleFavoritePositionUseCase: ToggleFavoritePosition
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
                val battingSelection = decodePlayerSideFlagUseCase(player.batting)
                val pitchingSelection = decodePlayerSideFlagUseCase(player.pitching)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = player.name,
                        shirtNumberText = player.shirtNumber.takeIf { n -> n > 0 }?.toString() ?: "",
                        licenseNumberText = player.licenseNumber.takeIf { n -> n > 0 }?.toString()
                            ?: "",
                        imageUri = player.image?.let(Uri::parse),
                        sex = Sex.getById(player.sex),
                        battingLeft = battingSelection.left,
                        battingRight = battingSelection.right,
                        pitchingLeft = pitchingSelection.left,
                        pitchingRight = pitchingSelection.right,
                        selectedPositions = FieldPosition.availablePositions
                            .filter { position -> position.matches(player.positions) }
                            .toSet(),
                        email = player.email ?: "",
                        phone = player.phone ?: "",
                        battingSide = PlayerSide.getSideByValue(player.batting),
                        pitchingSide = PlayerSide.getSideByValue(player.pitching)
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
        _uiState.update {
            val left = !it.battingLeft
            it.copy(
                battingLeft = left,
                battingSide = encodedSide(left, it.battingRight)
            )
        }
    }

    fun onBattingRightToggled() {
        _uiState.update {
            val right = !it.battingRight
            it.copy(
                battingRight = right,
                battingSide = encodedSide(it.battingLeft, right)
            )
        }
    }

    fun onPitchingLeftToggled() {
        _uiState.update {
            val left = !it.pitchingLeft
            it.copy(
                pitchingLeft = left,
                pitchingSide = encodedSide(left, it.pitchingRight)
            )
        }
    }

    fun onPitchingRightToggled() {
        _uiState.update {
            val right = !it.pitchingRight
            it.copy(
                pitchingRight = right,
                pitchingSide = encodedSide(it.pitchingLeft, right)
            )
        }
    }

    private fun encodedSide(left: Boolean, right: Boolean): PlayerSide? =
        PlayerSide.getSideByValue(encodePlayerSideFlagUseCase(left, right))

    fun onPositionToggled(position: FieldPosition) {
        _uiState.update {
            it.copy(
                selectedPositions = toggleFavoritePositionUseCase(it.selectedPositions, position)
            )
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
            current.selectedPositions.toPositionsMask(),
            encodePlayerSideFlagUseCase(current.pitchingLeft, current.pitchingRight),
            encodePlayerSideFlagUseCase(current.battingLeft, current.battingRight),
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
}
