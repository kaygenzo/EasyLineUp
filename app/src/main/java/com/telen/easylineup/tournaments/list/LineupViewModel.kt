/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MapInfo
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineupsUseCase
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

import kotlinx.coroutines.flow.MutableSharedFlow

sealed class SaveResult

/**
 * @property lineup
 */
data class SaveSuccess(val lineup: Lineup) : SaveResult()

class LineupViewModel : ViewModel(), KoinComponent {
    private val useCaseHandler: UseCaseHandler by inject()
    private val getTeamUseCase: GetTeam by inject()
    private val observeTournamentsUseCase: ObserveTournaments by inject()
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineupsUseCase by inject()
    private val getTournamentMapLink: GetTournamentMapLink by inject()
    private val deleteTournamentLineups: DeleteTournamentLineups by inject()
    private val saveTournamentUseCase: SaveTournament by inject()
    private val getRosterUseCase: GetRoster by inject()
    private val createLineupUseCase: CreateLineup by inject()
    private val prefsHelper by inject<SharedPreferencesHelper>()
    private val errors: Subject<DomainErrors.Lineups> = PublishSubject.create()
    private val _categorizedLineupsLiveData: MutableLiveData<List<TournamentItem>> =
        MutableLiveData()
    private val tournamentItems: MutableList<TournamentItem> = mutableListOf()
    private val filterLiveData: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    private var chosenRoster: TeamRosterSummary =
        TeamRosterSummary(Constants.STATUS_ALL, mutableListOf())
    private val saveResult: MutableLiveData<SaveResult> = MutableLiveData()
    private val disposables = CompositeDisposable()
    private var tournament: Tournament? = null
    private val lineup = Lineup()
    private val remoteConfig = Firebase.remoteConfig
    val mapsFlow: MutableSharedFlow<Pair<Tournament, MapInfo>> =
        MutableSharedFlow(extraBufferCapacity = 1, replay = 1)

    fun setFilter(filter: String) {
        filterLiveData.value = filter
    }

    fun registerSaveResults(): LiveData<SaveResult> {
        return saveResult
    }

    fun getTournaments(): LiveData<List<Tournament>> {
        return observeTournamentsUseCase.execute()
    }

    fun observeCategorizedLineups(): LiveData<List<TournamentItem>> {
        return filterLiveData.switchMap { filter ->
            _categorizedLineupsLiveData.apply {
                val disposable = useCaseHandler.execute(
                    getAllTournamentsWithLineupsUseCase,
                    GetAllTournamentsWithLineupsUseCase.RequestValues(filter)
                )
                    .map { it.result }
                    .flatMapObservable { Observable.fromIterable(it) }
                    .flatMapSingle { Single.just(TournamentItem(it.first, it.second)) }
                    .toList()
                    .subscribe({
                        tournamentItems.clear()
                        tournamentItems.addAll(it)
                        _categorizedLineupsLiveData.postValue(tournamentItems)
                        loadMaps(it)
                    }, {
                        Timber.e(it)
                    })
                disposables.add(disposable)
            }
        }
    }

    private fun loadMaps(tournamentItems: List<TournamentItem>) {
        val apiKey = remoteConfig.getString("maps_api_key")
        val items = tournamentItems.filter { it.tournament.address != null }
        val disposable = Observable.fromIterable(items)
            .flatMapSingle { item ->
                useCaseHandler.execute(
                    getTournamentMapLink,
                    GetTournamentMapLink.RequestValues(
                        item.tournament,
                        apiKey,
                        Constants.MAP_PIXEL_SIZE,
                        Constants.MAP_PIXEL_SIZE
                    )
                )
                    .map { Pair(item.tournament, it.mapInfo) }
                    .onErrorResumeNext { Single.just(Pair(item.tournament, MapInfo())) }
            }
            .filter { it.second.url?.isNotEmpty() ?: false }
            .subscribe({
                mapsFlow.tryEmit(it)
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
    }

    fun clear() {
        disposables.clear()
    }

    fun deleteTournament(tournament: Tournament): Completable {
        return useCaseHandler
            .execute(deleteTournamentLineups, DeleteTournamentLineups.RequestValues(tournament))
            .ignoreElement()
    }

    fun getCompleteRoster(): Single<TeamRosterSummary> {
        return useCaseHandler.execute(getRosterUseCase, GetRoster.RequestValues())
            .map { it.summary }
            .doOnSuccess { chosenRoster = it }
    }

    fun getChosenRoster(): Single<TeamRosterSummary> {
        return Single.just(chosenRoster)
    }

    fun saveLineup() {
        val disposable = useCaseHandler
            .execute(createLineupUseCase, CreateLineup.RequestValues(lineup, chosenRoster.players))
            .map { it.lineup }
            .doOnError {
                if (it is LineupNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_LINEUP_NAME)
                } else if (it is TournamentNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_TOURNAMENT_NAME)
                }
            }
            .subscribe({ saveResult.value = SaveSuccess(it) }, {
                when (it) {
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
            it.status = when (areSameSize) {
                true -> Constants.STATUS_ALL
                false -> Constants.STATUS_PARTIAL
            }
        }
    }

    fun showNewRosterFeature(): Single<Boolean> {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_NEW_ROSTER)
        if (show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_NEW_ROSTER)
        }
        return Single.just(show)
    }

    fun observeErrors(): Subject<DomainErrors.Lineups> {
        return errors
    }

    fun getTeamType(): Single<Int> {
        return useCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team.type }
    }

    fun saveTournament(tournament: Tournament): Completable {
        return useCaseHandler
            .execute(saveTournamentUseCase, SaveTournament.RequestValues(tournament))
            .ignoreElement()
    }

    fun onTournamentSelected(tournament: Tournament) {
        this.tournament = tournament
        this.lineup.tournamentId = tournament.id
    }

    fun onLineupStartTimeChanged(time: Long) {
        this.lineup.eventTimeInMillis = time
    }

    fun onLineupNameChanged(name: String) {
        this.lineup.name = name
    }

    fun onStrategyChanged(strategy: TeamStrategy) {
        this.lineup.strategy = strategy.id
    }

    fun onExtraHittersChanged(count: Int) {
        this.lineup.extraHitters = count
    }
}
