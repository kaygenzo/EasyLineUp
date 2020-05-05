package com.telen.easylineup.domain.model

data class PlayerInLineup(
        var lineupName: String = "",
        var lineupID: Long = 0,
        var position: Int?,
        var playerName: String?,
        var playerID: Long?
)