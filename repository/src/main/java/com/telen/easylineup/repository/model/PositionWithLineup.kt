package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo

data class PositionWithLineup(
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "order") var order: Int = 0,
        @ColumnInfo(name = "lineupName") var lineupName: String = "",
        @ColumnInfo(name = "tournamentName") var tournamentName: String = ""
)