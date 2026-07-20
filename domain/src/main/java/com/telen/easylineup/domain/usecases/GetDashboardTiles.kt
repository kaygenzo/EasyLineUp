/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.tiles.LastLineupData
import com.telen.easylineup.domain.model.tiles.LastPlayerNumberResearchData
import com.telen.easylineup.domain.model.tiles.MostUsedPlayerData
import com.telen.easylineup.domain.model.tiles.TeamSizeData
import com.telen.easylineup.domain.model.tiles.TileData
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TilesRepository
import kotlinx.coroutines.withContext

/**
 * Resolves the current team's dashboard tiles, auto-provisioning the default set the first
 * time a team has none.
 */
class GetDashboardTiles(
    private val playerDao: PlayerRepository,
    private val lineupDao: LineupRepository,
    private val playerFieldPositionDao: PlayerFieldPositionRepository,
    private val tilesRepo: TilesRepository,
    private val getTeam: GetTeam,
    private val createDashboardTiles: CreateDashboardTiles,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<List<DashboardTile>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val team = getTeam().getOrThrow()
            try {
                fetchTiles(team)
            } catch (e: NoSuchElementException) {
                createDashboardTiles().getOrThrow()
                fetchTiles(team)
            }
        }
    }

    private suspend fun fetchTiles(team: Team): List<DashboardTile> {
        val tiles = tilesRepo.getTiles()

        if (tiles.isEmpty()) {
            throw NoSuchElementException()
        }

        return tiles.mapNotNull { tile ->
            val data = when (tile.type) {
                TileType.TEAM_SIZE.type -> getTeamSize(team)
                TileType.MOST_USED_PLAYER.type -> getMostUsedPlayer(team)
                TileType.LAST_LINEUP.type -> getLastLineup(team)
                TileType.LAST_PLAYER_NUMBER.type -> getLastPlayerNumberResearch()
                else -> null
            }
            data?.let { tile.apply { this.data = it } }
        }
    }

    private suspend fun getMostUsedPlayer(team: Team): TileData? {
        val list = playerFieldPositionDao.getMostUsedPlayers(team.id)
        val mostUsed = list.firstOrNull() ?: return null
        val player = playerDao.getPlayerByIdAsSingle(mostUsed.playerId)
        return MostUsedPlayerData(player.image, player.name, player.shirtNumber, mostUsed.size)
    }

    private suspend fun getTeamSize(team: Team): TileData {
        val players = playerDao.getPlayersByTeamId(team.id)
        return TeamSizeData(players.size, teamType = team.type, teamImage = team.image)
    }

    private suspend fun getLastLineup(team: Team): TileData? {
        val lineup = lineupDao.getLastLineup(team.id) ?: return null
        val strategy = TeamStrategy.getStrategyById(lineup.strategy)
        return LastLineupData(lineup.id, lineup.name, strategy, lineup.extraHitters)
    }

    private fun getLastPlayerNumberResearch(): TileData {
        return LastPlayerNumberResearchData()
    }
}
