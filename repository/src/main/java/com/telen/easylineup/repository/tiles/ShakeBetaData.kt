package com.telen.easylineup.dashboard.models

import com.telen.easylineup.repository.model.Constants

class ShakeBetaData(): ITileData {

    override fun getData(): Map<Int, Any> {
        return mutableMapOf()
    }

    override fun getType(): Int {
        return Constants.TYPE_SHAKE_BETA
    }
}