/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.toRosterPlayerStatus
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.rxCompletable
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class LineupEditionViewModel : ViewModel(), KoinComponent {
    private val savePlayerNumberOverlayUseCase: SavePlayerNumberOverlay by inject()
    private val getTournamentsUseCase: GetTournaments by inject()
    private val getLineupByIdUseCase: GetLineupById by inject()
    private val getRosterUseCase: GetRoster by inject()
    private val updateLineupUseCase: UpdateLineup by inject()
    private val updateLineupRosterUseCase: UpdateLineupRoster by inject()
    var lineupId: Long = 0
        set(value) {
            field = value
            loadData()
        }
    private val rosterItems: MutableList<RosterItem> = mutableListOf()
    private val _rosterItemsFlow: MutableSharedFlow<List<RosterItem>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val _lineupFlow: MutableSharedFlow<Lineup> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private var lineup: Lineup? = null
    private val disposables = CompositeDisposable()

    private fun loadData() {
        val disposable = getLineupByIdUseCase(lineupId)
            .flatMap {
                this.lineup = it
                _lineupFlow.tryEmit(it)
                getRoster()
            }
            .map { it.map { RosterItem(it.player, it.status, it.playerNumberOverlay) } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                synchronized(rosterItems) {
                    rosterItems.run {
                        clear()
                        addAll(it)
                        _rosterItemsFlow.tryEmit(this)
                    }
                }
            }, { Timber.e(it) })
        disposables.add(disposable)
    }

    fun clear() {
        disposables.clear()
    }

    fun observeLineup(): Flow<Lineup> {
        return _lineupFlow
    }

    fun observeRosterItems(): Flow<List<RosterItem>> {
        return _rosterItemsFlow
    }

    private fun getRoster(): Single<List<RosterPlayerStatus>> {
        return getRosterUseCase(lineupId)
            .map { it.players }
    }

    fun saveClicked(): Completable {
        return Completable.defer {
            lineup?.let {
                updateLineupUseCase(it)
                    .andThen(
                        updateLineupRosterUseCase(
                            lineupId,
                            rosterItems.map { it.toRosterPlayerStatus() }
                        )
                    )
                    .andThen(rxCompletable { savePlayerNumberOverlayUseCase(rosterItems).getOrThrow() })
            } ?: Completable.error(LineupNameEmptyException())
        }
    }

    fun numberChanged(player: Player, number: Int) {
        synchronized(rosterItems) {
            rosterItems.firstOrNull { it.player.id == player.id }?.let { rosterItem ->
                rosterItem.playerNumberOverlay?.let {
                    it.number = number
                } ?: let {
                    rosterItem.playerNumberOverlay = PlayerNumberOverlay(
                        lineupId = lineupId,
                        playerId = rosterItem.player.id,
                        number = number
                    )
                }
            }
        }
    }

    fun playerSelectStatusChanged(player: Player, state: Boolean) {
        synchronized(rosterItems) {
            rosterItems.firstOrNull { it.player.id == player.id }?.selected = state
            _rosterItemsFlow.tryEmit(rosterItems)
        }
    }

    fun onLineupNameChanged(name: String) {
        lineup?.name = name
    }

    suspend fun getTournaments(): Result<List<Tournament>> {
        return getTournamentsUseCase()
    }

    fun onTournamentChanged(tournament: Tournament) {
        lineup?.tournamentId = tournament.id
    }
}
