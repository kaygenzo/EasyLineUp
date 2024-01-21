/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants

class ShakeBetaData : TileData {
    override fun getData(): Map<Int, Any> {
        return mutableMapOf()
    }

    override fun getType(): Int {
        return Constants.TYPE_SHAKE_BETA
    }
}
