package com.telen.easylineup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Team
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

sealed class Event
data class GetTeamSuccess(val team: Team): Event()
object GetTeamFailure : Event()
data class GetTeamsCountSuccess(val count: Int) : Event()
object GetTeamsCountFailure : Event()
object UpdateCurrentTeamSuccess : Event()
object UpdateCurrentTeamFailure : Event()
data class SwapButtonSuccess(val teams: List<Team>): Event()
object SwapButtonFailure: Event()

class HomeViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

    private val _event = PublishSubject.create<Event>()

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


    //TODO use event
    fun showNewSwapTeamFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, true)
        if(show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_NEW_SWAP_TEAM, false).apply()
        }
        return Single.just(show)
    }

}