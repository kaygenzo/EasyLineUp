package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.*
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TilesRepository
import io.reactivex.Maybe
import io.reactivex.Single

internal class GetDashboardTiles(private val playerDao: PlayerRepository,
                                 private val lineupDao: LineupRepository,
                                 private val playerFieldPositionDao: PlayerFieldPositionRepository,
                                 private val tilesRepo: TilesRepository
): UseCase<GetDashboardTiles.RequestValues, GetDashboardTiles.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return tilesRepo.getTiles().flatMap { tiles ->

            if(tiles.isEmpty()) {
                val resultError: Single<ResponseValue> = Single.error(NoSuchElementException())
                return@flatMap resultError
            }

            val tilesObservables = ArrayList<Maybe<DashboardTile>>()
            tiles.forEach { tile ->
                when (tile.type) {
                    TileType.TEAM_SIZE.type -> {
                        tilesObservables.add(getTeamSize(requestValues.team).map { tile.apply { data = it } })
                    }
                    TileType.MOST_USED_PLAYER.type -> {
                        tilesObservables.add(getMostUsedPlayer(requestValues.team).map { tile.apply { data = it } })
                    }
                    TileType.LAST_LINEUP.type -> {
                        tilesObservables.add(getLastLineup(requestValues.team).map { tile.apply { data = it } })
                    }
                    TileType.BETA.type -> {
                        tilesObservables.add(getShakeBeta().map { tile.apply { data = it } })
                    }
                }
            }
            Maybe.concat(tilesObservables)
                    .toList()
                    .map { ResponseValue(it) }
        }
    }

    private fun getMostUsedPlayer(team: Team): Maybe<ITileData> {
        return playerFieldPositionDao.getMostUsedPlayers(team.id).toMaybe()
                .flatMap { list ->
                    try {
                        val mostUsed = list.first()
                        playerDao.getPlayerByIdAsSingle(mostUsed.playerID)
                                .toMaybe()
                                .map { player -> MostUsedPlayerData(player.image, player.name, player.shirtNumber, mostUsed.size) }
                    } catch (e: NoSuchElementException) {
                        e.printStackTrace()
                        Maybe.empty<ITileData>()
                    }
                }
    }

    private fun getTeamSize(team: Team): Maybe<ITileData> {
        return playerDao.getPlayers(team.id)
                .toMaybe()
                .map { TeamSizeData(it.size, teamImage = team.image) }
    }

    private fun getLastLineup(team: Team): Maybe<ITileData> {
        return lineupDao.getLastLineup(team.id)
                .flatMap { lineup ->
                    playerFieldPositionDao.getAllPlayersWithPositionsForLineupRx(lineup.id)
                            .map { LastLineupData(lineup.id, lineup.name, it) }
                            .onErrorResumeNext {
                                it.printStackTrace()
                                Single.just(LastLineupData(lineup.id, lineup.name, listOf()))
                            }
                            .toMaybe()
                }
    }

    private fun getShakeBeta(): Maybe<ITileData> {
        return Maybe.just(ShakeBetaData())
    }

    class ResponseValue(val tiles: List<DashboardTile>): UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}