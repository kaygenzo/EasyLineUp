/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.repository.dao.TilesDao
import com.telen.easylineup.repository.model.RoomTile
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toDashboardTile

internal class TileRepositoryImpl(private val tilesDao: TilesDao) : TilesRepository {
    override suspend fun getTiles(): List<DashboardTile> {
        return tilesDao.getTiles().map { it.toDashboardTile() }
    }

    override suspend fun updateTiles(tiles: List<DashboardTile>) {
        tilesDao.updateTiles(tiles.map { RoomTile().init(it) })
    }

    override suspend fun createTiles(tiles: List<DashboardTile>) {
        tilesDao.insertTiles(tiles.map { RoomTile().init(it) })
    }
}
