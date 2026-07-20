/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TilesRepository
import kotlinx.coroutines.withContext

class CreateDashboardTiles(
    private val dao: TilesRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val tiles: MutableList<DashboardTile> = mutableListOf()
            tiles.add(DashboardTile(0, 1, TileType.TEAM_SIZE.type, true))
            tiles.add(DashboardTile(0, 2, TileType.MOST_USED_PLAYER.type, true))
            tiles.add(DashboardTile(0, 3, TileType.LAST_LINEUP.type, true))
            tiles.add(DashboardTile(0, 4, TileType.LAST_PLAYER_NUMBER.type, true))
            dao.createTiles(tiles)
        }
    }
}
