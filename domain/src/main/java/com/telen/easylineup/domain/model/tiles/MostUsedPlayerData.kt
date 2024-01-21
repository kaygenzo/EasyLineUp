/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.tiles

import com.telen.easylineup.domain.Constants

const val KEY_DATA_IMAGE = 0
const val KEY_DATA_NAME = 1
const val KEY_DATA_SHIRT_NUMBER = 2
const val KEY_DATA_MATCH_PLAYED = 3

/**
 * @property image
 * @property name
 * @property shirtNumber
 * @property matchPlayed
 */
class MostUsedPlayerData(
    val image: String?,
    val name: String,
    val shirtNumber: Int,
    val matchPlayed: Int
) : TileData {
    override fun getData(): Map<Int, Any> {
        val map: MutableMap<Int, Any> = mutableMapOf()
        image?.let {
            map[KEY_DATA_IMAGE] = it
        }
        map[KEY_DATA_MATCH_PLAYED] = matchPlayed
        map[KEY_DATA_SHIRT_NUMBER] = shirtNumber
        map[KEY_DATA_NAME] = name

        return map
    }

    override fun getType(): Int {
        return Constants.TYPE_MOST_USED_PLAYER
    }
}
