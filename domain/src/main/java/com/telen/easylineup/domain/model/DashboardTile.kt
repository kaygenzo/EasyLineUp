/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.tiles.TileData

/**
 * @property id
 * @property position
 * @property type
 * @property enabled
 * @property data
 */
data class DashboardTile(
    val id: Long = 0,
    val position: Int,
    val type: Int,
    val enabled: Boolean = true,
    val data: TileData? = null
)
