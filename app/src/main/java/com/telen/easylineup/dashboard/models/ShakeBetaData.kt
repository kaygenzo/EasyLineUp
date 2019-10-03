package com.telen.easylineup.dashboard.models

import com.telen.easylineup.dashboard.TYPE_SHAKE_BETA

class ShakeBetaData(): ITileData {

    override fun getData(): Map<Int, Any> {
        return mutableMapOf()
    }

    override fun getType(): Int {
        return TYPE_SHAKE_BETA
    }
}