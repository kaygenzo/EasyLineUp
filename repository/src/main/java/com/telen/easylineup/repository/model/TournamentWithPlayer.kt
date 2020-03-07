package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo

data class PlayerInLineup(
        @ColumnInfo(name = "lineupName") var lineupName: String = "",
        @ColumnInfo(name = "lineupID") var lineupID: Long = 0,
        @ColumnInfo(name = "position") var position: Int?,
        @ColumnInfo(name = "playerName") var playerName: String?,
        @ColumnInfo(name = "playerID") var playerID: Long?
)