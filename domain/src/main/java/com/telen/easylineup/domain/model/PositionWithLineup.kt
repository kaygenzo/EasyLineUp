package com.telen.easylineup.domain.model

data class PositionWithLineup(
        var position: Int = 0,
        var x: Float = 0f,
        var y: Float = 0f,
        var order: Int = 0,
        var lineupName: String = "",
        var tournamentName: String = ""
)