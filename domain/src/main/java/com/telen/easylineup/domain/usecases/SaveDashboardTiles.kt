/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.repository.TilesRepository
import io.reactivex.rxjava3.core.Completable

class SaveDashboardTiles(
    private val dao: TilesRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(tiles: List<DashboardTile>): Completable {
        for (i in tiles.indices) {
            tiles[i].position = i
        }
        return dao.updateTiles(tiles).subscribeOn(schedulersProvider.io())
    }
}
