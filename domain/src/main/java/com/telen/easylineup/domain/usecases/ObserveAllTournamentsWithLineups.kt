/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentWithLineup
import com.telen.easylineup.domain.model.matchesSearch
import com.telen.easylineup.domain.model.toLineup
import com.telen.easylineup.domain.model.toTournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * The current team's tournaments, each with its lineups (most recent first), matching
 * [filter] on the tournament or lineup name. Re-emits whenever a lineup, tournament or field
 * position is written, so a list built on it never needs to be told to reload: a lineup
 * created, edited or deleted from any screen shows up on its own.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveAllTournamentsWithLineups(
    private val dao: LineupRepository,
    private val observeCurrentTeam: ObserveCurrentTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    /**
     * Follows the current team too: switching team switches the list, with no team it's empty.
     * [filter] keeps the lineups whose name, or whose tournament's name, matches it, along with a
     * matching tournament's lineups. It is applied here rather than in SQL, whose `LIKE` only
     * ignores the case of unaccented Latin letters.
     */
    operator fun invoke(filter: String): Flow<List<Pair<Tournament, List<Lineup>>>> =
        observeCurrentTeam()
            .flatMapLatest { team ->
                if (team == null) {
                    flowOf(emptyList())
                } else {
                    dao.observeAllTournamentsWithLineups(team.id).map { rows ->
                        groupByTournament(rows.filter { it.matchesSearch(filter) })
                    }
                }
            }
            .flowOn(dispatcherProvider.io())

    private fun TournamentWithLineup.matchesSearch(filter: String): Boolean =
        tournamentName.matchesSearch(filter) || lineupName.orEmpty().matchesSearch(filter)

    private fun groupByTournament(
        items: List<TournamentWithLineup>
    ): List<Pair<Tournament, List<Lineup>>> {
        val result: MutableMap<Tournament, MutableList<Lineup>> = mutableMapOf()
        val lineups: MutableMap<Long, Lineup> = mutableMapOf()

        items.forEach { item ->
            val tournament = item.toTournament()
            val lineup = item.toLineup()

            result[tournament] ?: run {
                result[tournament] = mutableListOf()
            }

            if (lineup.id > 0 && lineups[lineup.id] == null) {
                lineups[lineup.id] = lineup
                result[tournament]?.add(lineup)
            }
        }

        return result.map { (tournament, tournamentLineups) ->
            val sortedLineups = tournamentLineups.sortedByDescending {
                it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis
            }
            tournament to sortedLineups
        }
    }
}
