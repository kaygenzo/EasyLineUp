/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.repository.dao.TilesDao
import com.telen.easylineup.repository.model.toDomain
import com.telen.easylineup.repository.model.toRoom

internal class TileRepositoryImpl(private val tilesDao: TilesDao) : TilesRepository {
    override suspend fun getTiles(): List<DashboardTile> {
        return tilesDao.getTiles().map { it.toDomain() }
    }

    override suspend fun updateTiles(tiles: List<DashboardTile>) {
        tilesDao.updateTiles(tiles.map { it.toRoom() })
    }

    override suspend fun createTiles(tiles: List<DashboardTile>) {
        tilesDao.insertTiles(tiles.map { it.toRoom() })
    }
}
