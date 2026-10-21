/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class LineupCreationEvent
data class LineupSaved(val lineupId: Long) : LineupCreationEvent()
object InvalidLineupName : LineupCreationEvent()
object InvalidTournament : LineupCreationEvent()

class LineupCreationViewModel(
    private val observeTournaments: ObserveTournaments,
    private val getTeam: GetTeam,
    private val getRoster: GetRoster,
    private val createLineup: CreateLineup,
    private val saveTournament: SaveTournament
) : ViewModel() {
    private val _events = Channel<LineupCreationEvent>(Channel.BUFFERED)
    val events: Flow<LineupCreationEvent> = _events.receiveAsFlow()

    fun getTournaments(): Flow<List<Tournament>> = observeTournaments().catch { Timber.e(it) }

    suspend fun getTeamType(): Result<Int> = getTeam().mapCatching { it.type }

    suspend fun loadRoster(): Result<TeamRosterSummary> = getRoster()

    suspend fun saveTournamentInline(tournament: Tournament): Result<Unit> =
        saveTournament(tournament)

    fun save(lineup: Lineup, roster: TeamRosterSummary) {
        viewModelScope.launch {
            createLineup(lineup, roster.players)
                .onSuccess { _events.send(LineupSaved(it.id)) }
                .onFailure {
                    when (it) {
                        is LineupNameEmptyException -> _events.send(InvalidLineupName)
                        is TournamentNameEmptyException -> _events.send(InvalidTournament)
                        else -> Timber.e(it)
                    }
                }
        }
    }
}
