/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
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
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<List<DashboardTile>> {
        return getTeam()
            .flatMap { team ->
                fetchTiles(team).onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        createDashboardTiles()
                            .andThen(fetchTiles(team))
                    } else {
                        Single.error(it)
                    }
                }
            }
            .subscribeOn(schedulersProvider.io())
    }

    private fun fetchTiles(team: Team): Single<List<DashboardTile>> {
        return tilesRepo.getTiles().flatMap { tiles ->

            if (tiles.isEmpty()) {
                val resultError: Single<List<DashboardTile>> = Single.error(NoSuchElementException())
                return@flatMap resultError
            }

            val tilesObservables: MutableList<Maybe<DashboardTile>> = mutableListOf()
            tiles.forEach { tile ->
                when (tile.type) {
                    TileType.TEAM_SIZE.type -> tilesObservables.add(getTeamSize(team).map {
                        tile.apply {
                            data = it
                        }
                    })
                    TileType.MOST_USED_PLAYER.type -> tilesObservables.add(getMostUsedPlayer(team).map {
                        tile.apply {
                            data = it
                        }
                    })
                    TileType.LAST_LINEUP.type -> tilesObservables.add(getLastLineup(team).map {
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
}
