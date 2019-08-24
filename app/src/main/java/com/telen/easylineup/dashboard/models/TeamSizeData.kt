package com.telen.easylineup.dashboard.models

import com.telen.easylineup.dashboard.TYPE_TEAM_SIZE

const val KEY_DATA_SIZE = 0

class TeamSizeData(val size: Int): ITileData {

    override fun getData(): Map<Int, Any> {
        val map = mutableMapOf<Int, Any>()
        map.put(KEY_DATA_SIZE, size)
        return map
    }

    override fun getType(): Int {
        return TYPE_TEAM_SIZE
    }
}