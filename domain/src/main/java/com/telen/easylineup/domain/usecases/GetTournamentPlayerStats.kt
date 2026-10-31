/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerTournamentStats
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import kotlinx.coroutines.withContext

class GetTournamentPlayerStats(
    private val stringResourcesProvider: StringResourcesProvider,
    private val dao: LineupRepository,
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(tournament: Tournament): Result<List<PlayerTournamentStats>> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                val team = getTeam().getOrThrow()
                val list = dao.getAllPlayerPositionsForTournament(tournament.id, team.id)

                val playerIdToName: MutableMap<Long, String> = mutableMapOf()
                val playerIdToPositions: MutableMap<Long, MutableList<FieldPosition>> =
                    mutableMapOf()

                list.forEach { entry ->
                    entry.playerId?.let { playerId ->
                        playerIdToName[playerId] = entry.playerName
                            ?: stringResourcesProvider.unknownPlayerName()

                        val position = entry.position?.let(FieldPosition::getFieldPositionById)
                        if (position != null && !position.isSubstitute()) {
                            playerIdToPositions.getOrPut(playerId) { mutableListOf() }.add(position)
                        }
                    }
                }

                playerIdToName.map { (playerId, name) ->
                    val positions = playerIdToPositions[playerId].orEmpty()
                    PlayerTournamentStats(
                        playerId = playerId,
                        playerName = name,
                        totalGames = positions.size,
                        positionCounts = positions.groupingBy { it }.eachCount()
                    )
                }
            }
        }
}
