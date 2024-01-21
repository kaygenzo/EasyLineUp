/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamStrategy

const val KEY_LINEUP_ID = 0
const val KEY_LINEUP_NAME = 1
const val KEY_LINEUP_STRATEGY = 3
const val KEY_LINEUP_EXTRA_HITTERS = 4

class LastLineupData(
    private val lineupId: Long,
    private val name: String,
    private val strategy: TeamStrategy,
    private val extraHitters: Int
) : TileData {
    override fun getData(): Map<Int, Any> {
        val map: MutableMap<Int, Any> = mutableMapOf()
        map[KEY_LINEUP_ID] = lineupId
        map[KEY_LINEUP_NAME] = name
        map[KEY_LINEUP_STRATEGY] = strategy
        map[KEY_LINEUP_EXTRA_HITTERS] = extraHitters
        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_LAST_LINEUP
    }
}
