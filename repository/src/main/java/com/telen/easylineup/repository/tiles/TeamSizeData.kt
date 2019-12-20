package com.telen.easylineup.repository.tiles

import com.telen.easylineup.repository.model.Constants

const val KEY_DATA_SIZE = 0
const val KEY_DATA_TEAM_IMAGE = 1

class TeamSizeData(val size: Int, val teamImage: String?): ITileData {

    override fun getData(): Map<Int, Any> {
        val map = mutableMapOf<Int, Any>()
        map.put(KEY_DATA_SIZE, size)
        teamImage?.let { map.put(KEY_DATA_TEAM_IMAGE, it) }
        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_TEAM_SIZE
    }
}