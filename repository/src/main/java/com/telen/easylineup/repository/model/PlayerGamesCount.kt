package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo

data class PlayerGamesCount (
        @ColumnInfo(name = "playerID") var playerID: Long = 0,
        @ColumnInfo(name = "size") var size: Int = 0
)