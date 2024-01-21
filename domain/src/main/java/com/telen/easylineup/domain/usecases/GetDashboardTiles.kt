/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.tiles.LastLineupData
import com.telen.easylineup.domain.model.tiles.LastPlayerNumberResearchData
import com.telen.easylineup.domain.model.tiles.MostUsedPlayerData
import com.telen.easylineup.domain.model.tiles.TeamSizeData
import com.telen.easylineup.domain.model.tiles.TileData
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TilesRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

internal class GetDashboardTiles(
    private val playerDao: PlayerRepository,
    private val lineupDao: LineupRepository,
    private val playerFieldPositionDao: PlayerFieldPositionRepository,
    private val tilesRepo: TilesRepository
) : UseCase<GetDashboardTiles.RequestValues, GetDashboardTiles.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return tilesRepo.getTiles().flatMap { tiles ->

            if (tiles.isEmpty()) {
                val resultError: Single<ResponseValue> = Single.error(NoSuchElementException())
                return@flatMap resultError
            }

            val tilesObservables: MutableList<Maybe<DashboardTile>> = mutableListOf()
            tiles.forEach { tile ->
                when (tile.type) {
                    TileType.TEAM_SIZE.type -> tilesObservables.add(getTeamSize(requestValues.team).map {
                        tile.apply {
                            data = it
                        }
                    })
                    TileType.MOST_USED_PLAYER.type -> tilesObservables.add(getMostUsedPlayer(requestValues.team).map {
                        tile.apply {
                            data = it
                        }
                    })
                    TileType.LAST_LINEUP.type -> tilesObservables.add(getLastLineup(requestValues.team).map {
                        tile.apply {
                            data = it
                        }
                    })
                    TileType.LAST_PLAYER_NUMBER.type -> tilesObservables.add(getLastPlayerNumberResearch().map {
                        tile.apply {
                            data = it
                        }
                    })
                }
            }
            Maybe.concat(tilesObservables)
                .toList()
                .map { ResponseValue(it) }
        }
    }

    private fun getMostUsedPlayer(team: Team): Maybe<TileData> {
        return playerFieldPositionDao.getMostUsedPlayers(team.id).toMaybe()
            .flatMap { list ->
                try {
                    val mostUsed = list.first()
                    playerDao.getPlayerByIdAsSingle(mostUsed.playerId)
                        .toMaybe()
                        .map { player ->
                            MostUsedPlayerData(
                                player.image,
                                player.name,
                                player.shirtNumber,
                                mostUsed.size
                            )
                        }
                } catch (e: NoSuchElementException) {
                    e.printStackTrace()
                    Maybe.empty()
                }
            }
    }

    private fun getTeamSize(team: Team): Maybe<TileData> {
        return playerDao.getPlayersByTeamId(team.id)
            .toMaybe()
            .map { TeamSizeData(it.size, teamType = team.type, teamImage = team.image) }
    }

    private fun getLastLineup(team: Team): Maybe<TileData> {
        return lineupDao.getLastLineup(team.id)
            .flatMap { lineup ->
                val strategy = TeamStrategy.getStrategyById(lineup.strategy)
                Maybe.just(LastLineupData(lineup.id, lineup.name, strategy, lineup.extraHitters))
            }
    }

    private fun getLastPlayerNumberResearch(): Maybe<TileData> {
        return Maybe.just(LastPlayerNumberResearchData())
    }

    /**
     * @property tiles
     */
    class ResponseValue(val tiles: List<DashboardTile>) : UseCase.ResponseValue
    /**
     * @property team
     */
    class RequestValues(val team: Team) : UseCase.RequestValues
}
