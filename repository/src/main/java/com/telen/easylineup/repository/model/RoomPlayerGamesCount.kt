package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PlayerGamesCount

internal data class RoomPlayerGamesCount (
        @ColumnInfo(name = "playerID") var playerID: Long = 0,
        @ColumnInfo(name = "size") var size: Int = 0
)

internal fun RoomPlayerGamesCount.toPlayerGamesCount(): PlayerGamesCount {
    return PlayerGamesCount(playerID, size)
}