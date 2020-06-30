package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.repository.dao.TilesDao
import com.telen.easylineup.repository.model.RoomTile
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toDashboardTile
import io.reactivex.Completable
import io.reactivex.Single

internal class TileRepositoryImpl(private val tilesDao: TilesDao): TilesRepository {
    override fun getTiles(): Single<List<DashboardTile>> {
        return tilesDao.getTiles().map { it.map { it.toDashboardTile() } }
    }

    override fun updateTiles(tiles: List<DashboardTile>): Completable {
        return tilesDao.updateTiles(tiles.map { RoomTile().init(it) })
    }

    override fun createTiles(tiles: List<DashboardTile>): Completable {
        return tilesDao.insertTiles(tiles.map { RoomTile().init(it) })
    }
}