/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import io.reactivex.rxjava3.schedulers.Schedulers
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
    private val _rosterItemsLiveData: MutableLiveData<List<RosterItem>> = MutableLiveData()
    private val _lineupLiveData: MutableLiveData<Lineup> = MutableLiveData()
    private var lineup: Lineup? = null

    private fun loadData() {
        getLineupByIdUseCase(lineupId)
            .flatMap {
                this.lineup = it
                _lineupLiveData.postValue(it)
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
                        _rosterItemsLiveData.postValue(this)
                    }
                }
            }, { Timber.e(it) })
    }

    fun observeLineup(): LiveData<Lineup> {
        return _lineupLiveData
    }

    fun observeRosterItems(): LiveData<List<RosterItem>> {
        return _rosterItemsLiveData
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
                    .andThen(savePlayerNumberOverlayUseCase(rosterItems))
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
            _rosterItemsLiveData.postValue(rosterItems)
        }
    }

    fun onLineupNameChanged(name: String) {
        lineup?.name = name
    }

    fun getTournaments(): Single<List<Tournament>> {
        return getTournamentsUseCase()
    }

    fun onTournamentChanged(tournament: Tournament) {
        lineup?.tournamentId = tournament.id
    }
}
