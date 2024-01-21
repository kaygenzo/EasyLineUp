/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.repository.TilesRepository
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class SaveDashboardTiles(val dao: TilesRepository) :
    UseCase<SaveDashboardTiles.RequestValues, SaveDashboardTiles.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.tiles)
            .flatMapCompletable { tiles ->
                for (i in tiles.indices) {
                    tiles[i].position = i
                }
                dao.updateTiles(tiles)
            }
            .andThen(Single.just(ResponseValue()))
    }

    class ResponseValue : UseCase.ResponseValue

    /**
     * @property tiles
     */
    class RequestValues(val tiles: List<DashboardTile>) : UseCase.RequestValues
}
