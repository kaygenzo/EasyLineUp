/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class Event
object UpdateCurrentTeamSuccess : Event()
object UpdateCurrentTeamFailure : Event()
object SwapButtonFailure : Event()

/** @property swapTeams the teams the swap-team sheet offers, or null while it is closed */
data class HomeUiState(val swapTeams: List<Team>? = null)

class HomeViewModel(
    private val getAllTeams: GetAllTeams,
    private val saveCurrentTeam: SaveCurrentTeam,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Only whether the sheet was open survives process death: its teams are read again.
        if (savedState.get<Boolean>(KEY_SWAP_SHEET_OPEN) == true) {
            onSwapButtonClicked()
        }
    }

    fun onSwapButtonClicked() {
        viewModelScope.launch {
            getAllTeams()
                .onSuccess { teams ->
                    savedState[KEY_SWAP_SHEET_OPEN] = true
                    _uiState.update { it.copy(swapTeams = teams) }
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(SwapButtonFailure)
                }
        }
    }

    fun onSwapTeamDismiss() {
        savedState[KEY_SWAP_SHEET_OPEN] = false
        _uiState.update { it.copy(swapTeams = null) }
    }

    fun updateCurrentTeam(currentTeam: Team) {
        viewModelScope.launch {
            saveCurrentTeam(currentTeam)
                .onSuccess {
                    _events.send(UpdateCurrentTeamSuccess)
                }
                .onFailure {
                    Timber.e(it)
                    _events.send(UpdateCurrentTeamFailure)
                }
        }
    }

    private companion object {
        const val KEY_SWAP_SHEET_OPEN = "swap_sheet_open"
    }
}
