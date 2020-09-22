package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants

class LastPlayerNumberResearchData(): ITileData {

    override fun getData(): Map<Int, Any> {
        return mutableMapOf()
    }

    override fun getType(): Int {
        return Constants.TYPE_LAST_PLAYER_NUMBER
    }
}