/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
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
    private val domain: ApplicationInteractor by inject()
    private val prefsHelper by inject<SharedPreferencesHelper>()
    private val _event: Subject<Event> = PublishSubject.create()
    val disposables = CompositeDisposable()

    fun registerTeamUpdates(): LiveData<List<Team>> {
        return domain.teams().observeTeams()
    }

    fun clear() {
        disposables.clear()
    }

    fun observeEvents(): Subject<Event> {
        return _event
    }

    fun getTeam() {
        val disposable = domain.teams().getTeam()
            .subscribe({
                _event.onNext(GetTeamSuccess(it))
            }, {
                Timber.e(it)
                _event.onNext(GetTeamFailure)
            })
        disposables.add(disposable)
    }

    fun getTeamsCount() {
        val disposable = domain.teams().getTeamsCount()
            .subscribe({
                _event.onNext(GetTeamsCountSuccess(it))
            }, {
                Timber.e(it)
                _event.onNext(GetTeamsCountFailure)
            })
        disposables.add(disposable)
    }

    fun onSwapButtonClicked() {
        val disposable = domain.teams().getAllTeams()
            .subscribe({
                _event.onNext(SwapButtonSuccess(it))
            }, {
                Timber.e(it)
                _event.onNext(SwapButtonFailure)
            })
        disposables.add(disposable)
    }

    fun updateCurrentTeam(currentTeam: Team) {
        val disposable = domain.teams().updateCurrentTeam(currentTeam)
            .subscribe({
                _event.onNext(UpdateCurrentTeamSuccess)
            }, {
                Timber.e(it)
                _event.onNext(UpdateCurrentTeamFailure)
            })
        disposables.add(disposable)
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
