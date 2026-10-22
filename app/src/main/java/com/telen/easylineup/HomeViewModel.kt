/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

sealed class Event
object UpdateCurrentTeamSuccess : Event()
object UpdateCurrentTeamFailure : Event()
/**
 * @property teams
 */
data class SwapButtonSuccess(val teams: List<Team>) : Event()
object SwapButtonFailure : Event()

class HomeViewModel : ViewModel(), KoinComponent {
    private val getAllTeams: GetAllTeams by inject()
    private val saveCurrentTeam: SaveCurrentTeam by inject()
    private val _event: Subject<Event> = PublishSubject.create()

    fun clear() {
    }

    fun observeEvents(): Subject<Event> {
        return _event
    }

    fun onSwapButtonClicked() {
        viewModelScope.launch {
            getAllTeams()
                .onSuccess { _event.onNext(SwapButtonSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    _event.onNext(SwapButtonFailure)
                }
        }
    }

    fun updateCurrentTeam(currentTeam: Team) {
        viewModelScope.launch {
            saveCurrentTeam(currentTeam)
                .onSuccess {
                    _event.onNext(UpdateCurrentTeamSuccess)
                }
                .onFailure {
                    Timber.e(it)
                    _event.onNext(UpdateCurrentTeamFailure)
                }
        }
    }
}
