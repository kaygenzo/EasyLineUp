package com.telen.easylineup.domain.model.tiles

interface ITileData {
    fun getType(): Int
    fun getData(): Map<Int, Any>
}