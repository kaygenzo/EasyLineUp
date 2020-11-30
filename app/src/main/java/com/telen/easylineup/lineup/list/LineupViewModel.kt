package com.telen.easylineup.lineup.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

sealed class SaveResult
data class SaveSuccess(val lineupID: Long, val lineupName: String, val strategy: TeamStrategy, val extraHitters: Int): SaveResult()

class LineupViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val _categorizedLineupsLiveData = MutableLiveData<List<Pair<Tournament, List<Lineup>>>>()

    private val filterLiveData: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    private var chosenRoster: TeamRosterSummary = TeamRosterSummary(Constants.STATUS_ALL, mutableListOf())

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

    fun getTypeType(): Single<Int>{
        return domain.getTeamType()
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

    fun getCompleteRoster(): Single<TeamRosterSummary> {
        return domain.getCompleteRoster().doOnSuccess { chosenRoster = it }
    }

    fun getChosenRoster(): Single<TeamRosterSummary> {
        return Single.just(chosenRoster)
    }

    fun saveLineup(tournament: Tournament, lineupTitle: String, lineupEventTime: Long, strategy: TeamStrategy, extraHitters: Int) {
        val disposable = domain.saveLineup(tournament, lineupTitle, chosenRoster, lineupEventTime, strategy, extraHitters)
                .subscribe({
                    saveResult.value = SaveSuccess(it, lineupTitle, strategy, extraHitters)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    fun rosterPlayerStatusChanged(position: Int, status: Boolean) {
        chosenRoster.let {
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

    fun observeErrors(): PublishSubject<DomainErrors> {
        return domain.observeErrors()
    }

    fun getTeamType(): Single<Int> {
        return domain.getTeamType()
    }
}