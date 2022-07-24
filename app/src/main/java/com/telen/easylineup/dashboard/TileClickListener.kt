package com.telen.easylineup.dashboard

import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.tiles.ITileData

interface TileClickListener {
    fun onTileClicked(type: Int, data: ITileData)
    fun onTileLongClicked(type: Int)
    fun onTileSearchNumberClicked(number: Int)
    fun onTileSearchNumberHistoryClicked(history: List<ShirtNumberEntry>)
    fun onTileTeamSizeSendButtonClicked()
}