/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.model.Tournament

/** [GetTournamentPlayerStats], enriched with each player's current shirt number/photo. */
class GetTournamentPlayerStatsWithProfiles(
    private val getTournamentPlayerStats: GetTournamentPlayerStats,
    private val getPlayers: GetPlayers
) {
    suspend operator fun invoke(
        tournament: Tournament
    ): Result<List<PlayerTournamentStatsProfile>> =
        getTournamentPlayerStats(tournament).mapCatching { stats ->
            val playersById = getPlayers().getOrThrow().associateBy { it.id }
            stats.map { stat ->
                val player = playersById[stat.playerId]
                PlayerTournamentStatsProfile(
                    playerId = stat.playerId,
                    playerName = stat.playerName,
                    shirtNumber = player?.shirtNumber ?: 0,
                    image = player?.image,
                    totalGames = stat.totalGames,
                    positionCounts = stat.positionCounts
                )
            }
        }
}
