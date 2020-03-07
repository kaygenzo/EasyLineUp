package com.telen.easylineup.repository.tiles

interface ITileData {
    fun getType(): Int
    fun getData(): Map<Int, Any>
}