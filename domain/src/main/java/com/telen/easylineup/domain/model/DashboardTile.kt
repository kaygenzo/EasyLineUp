package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.tiles.ITileData

data class DashboardTile(
        var id: Long = 0,
        var position: Int,
        val type: Int,
        val enabled: Boolean = true,
        var data: ITileData? = null
)