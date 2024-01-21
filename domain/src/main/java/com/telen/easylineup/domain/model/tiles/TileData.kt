/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.tiles

interface TileData {
    fun getType(): Int
    fun getData(): Map<Int, Any>
}
