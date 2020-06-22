package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerWithPosition

const val KEY_LINEUP_ID = 0
const val KEY_LINEUP_NAME = 1
const val KEY_LINEUP_PLAYERS = 2

class LastLineupData(private val lineupID: Long, private val name: String, private val playersWithPosition: List<PlayerWithPosition>): ITileData {

    override fun getData(): Map<Int, Any> {
        val map = mutableMapOf<Int, Any>()
        map[KEY_LINEUP_ID] = lineupID
        map[KEY_LINEUP_NAME] = name
        map[KEY_LINEUP_PLAYERS] = playersWithPosition
        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_LAST_LINEUP
    }
}