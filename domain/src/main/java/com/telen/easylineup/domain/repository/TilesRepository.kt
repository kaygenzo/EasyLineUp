package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.DashboardTile
import io.reactivex.Completable
import io.reactivex.Single

interface TilesRepository {
    fun getTiles(): Single<List<DashboardTile>>
    fun updateTiles(tiles: List<DashboardTile>): Completable
    fun createTiles(tiles: List<DashboardTile>): Completable
}