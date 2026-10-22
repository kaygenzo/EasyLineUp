/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class LineupEditionEvent
object LineupUpdated : LineupEditionEvent()
object InvalidLineupName : LineupEditionEvent()
object InvalidTournament : LineupEditionEvent()

class LineupEditionViewModel(
    private val getLineupById: GetLineupById,
    private val getRoster: GetRoster,
    private val updateLineup: UpdateLineup,
    private val updateLineupRoster: UpdateLineupRoster,
    private val savePlayerNumberOverlay: SavePlayerNumberOverlay,
    private val observeTournaments: ObserveTournaments
) : ViewModel() {
    private val _events = Channel<LineupEditionEvent>(Channel.BUFFERED)
    val events: Flow<LineupEditionEvent> = _events.receiveAsFlow()

    fun getTournaments(): Flow<List<Tournament>> = observeTournaments().catch { Timber.e(it) }

    suspend fun load(lineupId: Long): Result<Pair<Lineup, TeamRosterSummary>> {
        return getLineupById(lineupId).mapCatching { lineup ->
            lineup to getRoster(lineupId).getOrThrow()
        }
    }

    fun save(lineup: Lineup, roster: TeamRosterSummary) {
        viewModelScope.launch {
            when {
                lineup.name.isBlank() -> _events.send(InvalidLineupName)
                lineup.tournamentId <= 0 -> _events.send(InvalidTournament)
                else -> updateLineup(lineup)
                    .mapCatching {
                        updateLineupRoster(lineup.id, roster.players).getOrThrow()
                    }
                    .mapCatching {
                        savePlayerNumberOverlay(roster.players).getOrThrow()
                    }
                    .onSuccess { _events.send(LineupUpdated) }
                    .onFailure { Timber.e(it) }
            }
        }
    }
}
