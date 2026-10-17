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
const val KEY_LINEUP_EVENT_TIME = 5
const val KEY_LINEUP_TOURNAMENT_NAME = 6

/**
 * @property lineupId
 * @property name
 * @property strategy
 * @property extraHitters
 * @property eventTimeInMillis when this lineup is for, 0 if never set
 * @property tournamentName null when the lineup has no tournament attached
 */
class LastLineupData(
    val lineupId: Long,
    val name: String,
    val strategy: TeamStrategy,
    val extraHitters: Int,
    val eventTimeInMillis: Long,
    val tournamentName: String?
) : TileData {
    override fun getData(): Map<Int, Any> {
        val map: MutableMap<Int, Any> = mutableMapOf()
        map[KEY_LINEUP_ID] = lineupId
        map[KEY_LINEUP_NAME] = name
        map[KEY_LINEUP_STRATEGY] = strategy
        map[KEY_LINEUP_EXTRA_HITTERS] = extraHitters
        map[KEY_LINEUP_EVENT_TIME] = eventTimeInMillis
        tournamentName?.let { map[KEY_LINEUP_TOURNAMENT_NAME] = it }
        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_LAST_LINEUP
    }
}
