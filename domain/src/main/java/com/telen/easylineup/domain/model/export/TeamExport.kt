/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

data class TeamExport(
    val id: String,
    val name: String,
    var image: String?,
    val type: Int,
    val main: Boolean,
    val players: List<PlayerExport>,
    val tournaments: List<TournamentExport>
)