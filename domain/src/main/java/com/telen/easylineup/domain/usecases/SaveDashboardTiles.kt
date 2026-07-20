/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TilesRepository
import kotlinx.coroutines.withContext

class SaveDashboardTiles(
    private val dao: TilesRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(tiles: List<DashboardTile>): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            for (i in tiles.indices) {
                tiles[i].position = i
            }
            dao.updateTiles(tiles)
        }
    }
}
