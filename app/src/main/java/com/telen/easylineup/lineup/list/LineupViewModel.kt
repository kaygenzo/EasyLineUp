package com.telen.easylineup.lineup.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

sealed class SaveResult
data class SaveSuccess(val lineupID: Long, val lineupName: String): SaveResult()

class LineupViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val _categorizedLineupsLiveData = MutableLiveData<List<Pair<Tournament, List<Lineup>>>>()

    private val filterLiveData: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    private var chosenRoster: TeamRosterSummary? = null

    private val saveResult = MutableLiveData<SaveResult>()

    private val disposables = CompositeDisposable()

    fun setFilter(filter: String) {
        filterLiveData.value = filter
    }

    fun registerSaveResults(): LiveData<SaveResult> {
        return saveResult
    }

    fun getTournaments(): Single<List<Tournament>>{
        return domain.getTournaments()
    }

    fun observeCategorizedLineups(): LiveData<List<Pair<Tournament, List<Lineup>>>> {
        return Transformations.switchMap(filterLiveData) { filter ->
            val disposable = domain.getCategorizedLineups(filter)
                    .subscribe({
                        _categorizedLineupsLiveData.value = it
                    }, {
                        Timber.e(it)
                    })
            disposables.add(disposable)
            _categorizedLineupsLiveData
        }
    }

    fun clear() {
        disposables.clear()
    }

    fun deleteTournament(tournament: Tournament) : Completable {
        return domain.deleteTournament(tournament)
    }

    fun getRoster(): Single<TeamRosterSummary> {
        return chosenRoster?.let {
            Single.just(it)
        } ?: domain.getRoster().doOnSuccess { chosenRoster = it }
    }

    fun saveLineup(tournament: Tournament, lineupTitle: String) {
        val disposable = domain.saveLineup(tournament, lineupTitle)
                .subscribe({
                    saveResult.value = SaveSuccess(it, lineupTitle)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    fun rosterPlayerStatusChanged(position: Int, status: Boolean) {
        chosenRoster?.let {
            it.players[position].status = status
            val areSameSize = it.players.filter { it.status }.size == it.players.size
            it.status = when(areSameSize) {
                true -> Constants.STATUS_ALL
                false -> Constants.STATUS_PARTIAL
            }
        }
    }

    fun showNewRosterFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, true)
        if(show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, false).apply()
        }
        return Single.just(show)
    }

    fun observeErrors(): LiveData<DomainErrors> {
        return domain.observeErrors()
    }
}