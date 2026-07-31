/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

data class TournamentExport(
    val id: String,
    val name: String,
    val createdAt: Long,
    val startTime: Long,
    val endTime: Long,
    val address: String?,
    val lineups: List<LineupExport>
)
