package com.telen.easylineup.repository.tiles

import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.PlayerWithPosition

const val KEY_LINEUP_NAME = 0
const val KEY_LINEUP_PLAYERS = 1

class LastLineupData(val name: String, val playersWithPosition: List<PlayerWithPosition>): ITileData {

    override fun getData(): Map<Int, Any> {
        val map = mutableMapOf<Int, Any>()
        map[KEY_LINEUP_NAME] = name
        map[KEY_LINEUP_PLAYERS] = playersWithPosition
        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_LAST_LINEUP
    }
}