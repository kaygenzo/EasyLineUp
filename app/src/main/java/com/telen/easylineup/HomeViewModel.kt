/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveTeams
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

sealed class Event
/**
 * @property team
 */
data class GetTeamSuccess(val team: Team) : Event()
object GetTeamFailure : Event()
/**
 * @property count
 */
data class GetTeamsCountSuccess(val count: Int) : Event()
object GetTeamsCountFailure : Event()
object UpdateCurrentTeamSuccess : Event()
object UpdateCurrentTeamFailure : Event()
/**
 * @property teams
 */
data class SwapButtonSuccess(val teams: List<Team>) : Event()
object SwapButtonFailure : Event()

class HomeViewModel : ViewModel(), KoinComponent {
    private val observeTeams: ObserveTeams by inject()
    private val getTeamUseCase: GetTeam by inject()
    private val getAllTeams: GetAllTeams by inject()
    private val saveCurrentTeam: SaveCurrentTeam by inject()
    private val prefsHelper by inject<SharedPreferencesHelper>()
    private val _event: Subject<Event> = PublishSubject.create()

    fun registerTeamUpdates(): Flow<List<Team>> {
        return observeTeams().catch { Timber.e(it) }
    }

    fun clear() {
    }

    fun observeEvents(): Subject<Event> {
        return _event
    }

    fun getTeam() {
        viewModelScope.launch {
            getTeamUseCase()
                .onSuccess { _event.onNext(GetTeamSuccess(it)) }
                .onFailure {
                    Timber.e(it)
                    _event.onNext(GetTeamFailure)
                }
        }
    }

    fun getTeamsCount() {
        viewModelScope.launch {
            getAllTeams()
                .onSuccess { _event.onNext(GetTeamsCountSuccess(it.size)) }
                .onFailure {
                    Timber.e(it)
                    _event.onNext(GetTeamsCountFailure)
                }
        }
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

    // TODO use event
    fun showNewSwapTeamFeature(): Single<Boolean> {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM)
        if (show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM)
        }
        return Single.just(show)
    }
}
