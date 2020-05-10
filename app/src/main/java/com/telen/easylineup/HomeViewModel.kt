package com.telen.easylineup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.utils.SingleLiveEvent
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
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
data class SwapButtonSuccess(val team: List<Team>): Event()
object SwapButtonFailure: Event()

class HomeViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val _event = SingleLiveEvent<Event>()

    val disposables = CompositeDisposable()

    fun registerTeamUpdates(): LiveData<List<Team>> {
        return domain.observeTeams()
    }

    fun clear() {
        disposables.clear()
    }

    fun observeEvents(): LiveData<Event> {
        return _event
    }

    fun getTeam() {
        val disposable = domain.getTeam()
                .subscribe({
                    _event.postValue(GetTeamSuccess(it))
                }, {
                    Timber.e(it)
                    _event.postValue(GetTeamFailure)
                })
        disposables.add(disposable)
    }

    fun getTeamsCount() {
        val disposable = domain.getTeamsCount()
                .subscribe({
                    _event.postValue(GetTeamsCountSuccess(it))
                }, {
                    Timber.e(it)
                    _event.postValue(GetTeamsCountFailure)
                })
        disposables.add(disposable)
    }

    fun onSwapButtonClicked() {
        val disposable = domain.getAllTeams()
                .subscribe({
                    _event.postValue(SwapButtonSuccess(it))
                }, {
                    Timber.e(it)
                    _event.postValue(SwapButtonFailure)
                })
        disposables.add(disposable)
    }

    fun updateCurrentTeam(currentTeam: Team) {
        val disposable = domain.updateCurrentTeam(currentTeam)
                .subscribe({
                    _event.postValue(UpdateCurrentTeamSuccess)
                }, {
                    Timber.e(it)
                    _event.postValue(UpdateCurrentTeamFailure)
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