/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

data class PlayerPositionExport(
    val id: String,
    val playerId: String?,
    val position: Int,
    val x: Float,
    val y: Float,
    val flags: Int,
    val order: Int
)
