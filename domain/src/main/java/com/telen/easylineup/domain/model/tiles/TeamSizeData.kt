package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants

const val KEY_DATA_SIZE = 0
const val KEY_DATA_TEAM_IMAGE = 1
const val KEY_DATA_TEAM_TYPE = 2

class TeamSizeData(val size: Int, val teamType: Int, val teamImage: String?): ITileData {

    override fun getData(): Map<Int, Any> {
        val map = mutableMapOf<Int, Any>()
        map[KEY_DATA_SIZE] = size
        map[KEY_DATA_TEAM_TYPE] = teamType
        teamImage?.let { map.put(KEY_DATA_TEAM_IMAGE, it) }
        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_TEAM_SIZE
    }
}