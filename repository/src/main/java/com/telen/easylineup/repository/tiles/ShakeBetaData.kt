package com.telen.easylineup.repository.tiles

import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.tiles.ITileData

class ShakeBetaData(): ITileData {

    override fun getData(): Map<Int, Any> {
        return mutableMapOf()
    }

    override fun getType(): Int {
        return Constants.TYPE_SHAKE_BETA
    }
}