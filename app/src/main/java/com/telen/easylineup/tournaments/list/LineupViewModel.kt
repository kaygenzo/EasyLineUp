package com.telen.easylineup.tournaments.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

sealed class SaveResult
data class SaveSuccess(val lineupID: Long, val lineupName: String, val strategy: TeamStrategy, val extraHitters: Int): SaveResult()

class LineupViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

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
        return domain.tournaments().getTournaments()
    }

    fun getTypeType(): Single<Int>{
        return domain.teams().getTeamType()
    }

    fun observeCategorizedLineups(): LiveData<List<Pair<Tournament, List<Lineup>>>> {
        return filterLiveData.switchMap { filter ->
            val disposable = domain.tournaments().getCategorizedLineups(filter)

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
        return domain.tournaments().deleteTournament(tournament)
    }

    fun getCompleteRoster(): Single<TeamRosterSummary> {
        return domain.lineups().getCompleteRoster().doOnSuccess { chosenRoster = it }
    }

    fun getChosenRoster(): Single<TeamRosterSummary> {
        return Single.just(chosenRoster)
    }

    fun saveLineup(tournament: Tournament, lineupTitle: String, lineupEventTime: Long, strategy: TeamStrategy, extraHitters: Int) {
        val disposable = domain.lineups().saveLineup(tournament, lineupTitle, chosenRoster, lineupEventTime, strategy, extraHitters)
                .subscribe({
                    saveResult.value = SaveSuccess(it, lineupTitle, strategy, extraHitters)
                }, {
                    when(it) {
                        is TournamentNameEmptyException,
                        is LineupNameEmptyException -> Timber.w(it.message)
                        else -> Timber.e(it)
                    }
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

    fun observeErrors(): Subject<DomainErrors.Lineups> {
        return domain.lineups().observeErrors()
    }

    fun getTeamType(): Single<Int> {
        return domain.teams().getTeamType()
    }
}