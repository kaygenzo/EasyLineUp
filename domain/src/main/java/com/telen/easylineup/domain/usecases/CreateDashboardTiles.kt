package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.TilesRepository
import io.reactivex.Single

internal class CreateDashboardTiles(val dao: TilesRepository): UseCase<CreateDashboardTiles.RequestValues, CreateDashboardTiles.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val tiles = mutableListOf<DashboardTile>()
        tiles.add(DashboardTile(0, 1, TileType.TEAM_SIZE.type, true))
        tiles.add(DashboardTile(0, 2, TileType.MOST_USED_PLAYER.type, true))
        tiles.add(DashboardTile(0, 3, TileType.LAST_LINEUP.type, true))
        tiles.add(DashboardTile(0, 4, TileType.LAST_PLAYER_NUMBER.type, true))
        return dao.createTiles(tiles).andThen(Single.just(ResponseValue()))
    }

    class ResponseValue: UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}