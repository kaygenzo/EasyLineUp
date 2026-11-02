/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class Event
object UpdateCurrentTeamSuccess : Event()
object UpdateCurrentTeamFailure : Event()
/**
 * @property teams
 */
data class SwapButtonSuccess(val teams: List<Team>) : Event()
object SwapButtonFailure : Event()

class HomeViewModel(
    private val getAllTeams: GetAllTeams,
    private val saveCurrentTeam: SaveCurrentTeam
) : ViewModel() {
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onSwapButtonClicked() {
        viewModelScope.launch {
            getAllTeams()
                .onSuccess { _events.send(SwapButtonSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    _events.send(SwapButtonFailure)
                }
        }
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
}
