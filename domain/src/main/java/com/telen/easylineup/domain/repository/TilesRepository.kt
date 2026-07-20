/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.DashboardTile

interface TilesRepository {
    suspend fun getTiles(): List<DashboardTile>
    suspend fun updateTiles(tiles: List<DashboardTile>)
    suspend fun createTiles(tiles: List<DashboardTile>)
}
