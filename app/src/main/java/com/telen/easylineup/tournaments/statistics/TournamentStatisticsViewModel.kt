/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetTournamentPlayerStats

class TournamentStatisticsViewModel(
    private val getTournamentPlayerStats: GetTournamentPlayerStats,
    private val getPlayers: GetPlayers
) : ViewModel() {
    suspend fun load(tournament: Tournament): Result<List<PlayerStatsRow>> {
        return getTournamentPlayerStats(tournament).mapCatching { stats ->
            val playersById = getPlayers().getOrThrow().associateBy { it.id }
            stats.map { stat ->
                val player = playersById[stat.playerId]
                PlayerStatsRow(
                    playerId = stat.playerId,
                    name = stat.playerName,
                    shirtNumber = player?.shirtNumber ?: 0,
                    imageUri = player?.image?.let { Uri.parse(it) },
                    totalGames = stat.totalGames,
                    positionCounts = stat.positionCounts
                )
            }
        }
    }
}
