/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.tiles

class LastPlayerNumberResearchData : TileData {
    private val data: MutableMap<Int, Any> = mutableMapOf()

    override fun getData(): Map<Int, Any> {
        return data
    }
}
