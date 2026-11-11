/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.model.ShirtNumberEntry

const val KEY_DATA_HISTORY = 0

class LastPlayerNumberResearchData : TileData {
    private val data: MutableMap<Int, Any> = mutableMapOf()

    override fun getData(): Map<Int, Any> {
        return data
    }

    fun setHistory(history: List<ShirtNumberEntry>) {
        data[KEY_DATA_HISTORY] = history
    }
}
