package com.telen.easylineup.dashboard.models

interface ITileData {
    fun getType(): Int
    fun getData(): Map<Int, Any>
}