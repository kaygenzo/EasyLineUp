/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.tournaments.list

import androidx.lifecycle.ViewModel

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.telen.easylineup.domain.Constants
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
import com.telen.easylineup.utils.asSafeFlow
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

sealed class SaveResult

/**
 * @property lineup
 */
data class SaveSuccess(val lineup: Lineup) : SaveResult()

class LineupViewModel : ViewModel(), KoinComponent {
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
    private val _categorizedLineupsFlow: MutableSharedFlow<List<TournamentItem>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val tournamentItems: MutableList<TournamentItem> = mutableListOf()
    private val filterFlow: MutableStateFlow<String> by lazy {
        MutableStateFlow("")
    }
    private var chosenRoster: TeamRosterSummary =
        TeamRosterSummary(Constants.STATUS_ALL, mutableListOf())
    private val saveResult: MutableSharedFlow<SaveResult> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val disposables = CompositeDisposable()
    private var tournament: Tournament? = null
    private val lineup = Lineup()
    private val remoteConfig = Firebase.remoteConfig
    val mapsFlow: MutableSharedFlow<Pair<Tournament, MapInfo>> =
        MutableSharedFlow(extraBufferCapacity = 1, replay = 1)

    fun setFilter(filter: String) {
        filterFlow.value = filter
    }

    fun registerSaveResults(): Flow<SaveResult> {
        return saveResult
    }

    fun getTournaments(): Flow<List<Tournament>> {
        return observeTournamentsUseCase().asSafeFlow()
    }

    fun observeCategorizedLineups(): Flow<List<TournamentItem>> {
        return filterFlow.flatMapLatest { filter ->
            _categorizedLineupsFlow.apply {
                val disposable = getAllTournamentsWithLineupsUseCase(filter)
                    .flatMapObservable { Observable.fromIterable(it) }
                    .flatMapSingle { Single.just(TournamentItem(it.first, it.second)) }
                    .toList()
                    .subscribe({
                        tournamentItems.clear()
                        tournamentItems.addAll(it)
                        _categorizedLineupsFlow.tryEmit(tournamentItems)
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
                getTournamentMapLink(
                    item.tournament,
                    apiKey,
                    Constants.MAP_PIXEL_SIZE,
                    Constants.MAP_PIXEL_SIZE
                )
                    .map { Pair(item.tournament, it) }
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
        return deleteTournamentLineups(tournament)
    }

    fun getCompleteRoster(): Single<TeamRosterSummary> {
        return getRosterUseCase()
            .doOnSuccess { chosenRoster = it }
    }

    fun getChosenRoster(): Single<TeamRosterSummary> {
        return Single.just(chosenRoster)
    }

    fun saveLineup() {
        val disposable = createLineupUseCase(lineup, chosenRoster.players)
            .doOnError {
                if (it is LineupNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_LINEUP_NAME)
                } else if (it is TournamentNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_TOURNAMENT_NAME)
                }
            }
            .subscribe({ saveResult.tryEmit(SaveSuccess(it)) }, {
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
        return getTeamUseCase().map { it.type }
    }

    fun saveTournament(tournament: Tournament): Completable {
        return saveTournamentUseCase(tournament)
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
