/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

data class LineupExport(
    val id: String,
    val name: String,
    val eventTime: Long,
    val createdAt: Long,
    val editedAt: Long,
    val mode: Int,
    val strategy: Int,
    var extraHitters: Int,
    val roster: List<String>?,
    val playerPositions: List<PlayerPositionExport>,
    val playerNumberOverlays: List<PlayerNumberOverlayExport>
)
