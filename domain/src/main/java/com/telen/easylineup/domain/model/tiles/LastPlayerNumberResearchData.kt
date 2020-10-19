package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.ShirtNumberEntry

const val KEY_DATA_HISTORY = 0

class LastPlayerNumberResearchData(): ITileData {

    private val data = mutableMapOf<Int, Any>()

    override fun getData(): Map<Int, Any> {
        return data
    }

    override fun getType(): Int {
        return Constants.TYPE_LAST_PLAYER_NUMBER
    }

    fun setHistory(history: List<ShirtNumberEntry>) {
        data[KEY_DATA_HISTORY] = history
    }
}