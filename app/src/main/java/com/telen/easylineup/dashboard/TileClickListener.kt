/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.tiles.TileData

interface TileClickListener {
    fun onTileClicked(type: Int, data: TileData)
    fun onTileLongClicked(type: Int)
    fun onTileSearchNumberClicked(number: Int)
    fun onTileSearchNumberHistoryClicked(history: List<ShirtNumberEntry>)
    fun onTileTeamSizeSendButtonClicked()
}
