/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.toRosterPlayerStatus
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
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
    private var job: Job? = null

    private fun loadData() {
        job?.cancel()
        job = viewModelScope.launch {
            getLineupByIdUseCase(lineupId)
                .onSuccess { loadedLineup ->
                    this@LineupEditionViewModel.lineup = loadedLineup
                    _lineupFlow.tryEmit(loadedLineup)

                    getRosterUseCase(lineupId)
                        .onSuccess { roster ->
                            val items = roster.players.map {
                                RosterItem(it.player, it.status, it.playerNumberOverlay)
                            }
                            synchronized(rosterItems) {
                                rosterItems.clear()
                                rosterItems.addAll(items)
                                _rosterItemsFlow.tryEmit(rosterItems)
                            }
                        }
                        .onFailure { Timber.e(it) }
                }
                .onFailure { Timber.e(it) }
        }
    }

    fun clear() {
        job?.cancel()
    }

    fun observeLineup(): Flow<Lineup> {
        return _lineupFlow
    }

    fun observeRosterItems(): Flow<List<RosterItem>> {
        return _rosterItemsFlow
    }

    suspend fun saveClicked(): Result<Unit> {
        val currentLineup = lineup ?: return Result.failure(LineupNameEmptyException())
        return updateLineupUseCase(currentLineup)
            .mapCatching {
                updateLineupRosterUseCase(
                    lineupId,
                    rosterItems.map { it.toRosterPlayerStatus() }
                ).getOrThrow()
            }
            .mapCatching {
                savePlayerNumberOverlayUseCase(rosterItems).getOrThrow()
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
