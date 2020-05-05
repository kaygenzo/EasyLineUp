package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants

class ShakeBetaData(): ITileData {

    override fun getData(): Map<Int, Any> {
        return mutableMapOf()
    }

    override fun getType(): Int {
        return Constants.TYPE_SHAKE_BETA
    }
}