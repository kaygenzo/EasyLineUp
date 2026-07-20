/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import kotlinx.coroutines.withContext

class GetAllTournamentsWithLineupsUseCase(
    private val dao: LineupRepository,
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(filter: String): Result<List<Pair<Tournament, List<Lineup>>>> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                val team = getTeam().getOrThrow()
                val items = dao.getAllTournamentsWithLineups(filter, team.id)

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

                val list: MutableList<Pair<Tournament, List<Lineup>>> = mutableListOf()
                result.forEach { item ->
                    val tournament = item.key
                    val sortedLineups = item.value.sortedByDescending {
                        it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis
                    }
                    list.add(Pair(tournament, sortedLineups))
                }
                list as List<Pair<Tournament, List<Lineup>>>
            }
        }
}
