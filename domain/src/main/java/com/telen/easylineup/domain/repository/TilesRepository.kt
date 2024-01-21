/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.DashboardTile
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TilesRepository {
    fun getTiles(): Single<List<DashboardTile>>
    fun updateTiles(tiles: List<DashboardTile>): Completable
    fun createTiles(tiles: List<DashboardTile>): Completable
}
