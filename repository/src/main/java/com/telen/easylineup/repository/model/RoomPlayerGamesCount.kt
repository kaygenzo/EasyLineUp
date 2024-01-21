/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PlayerGamesCount

/**
 * @property playerId
 * @property size
 */
internal data class RoomPlayerGamesCount(
    @ColumnInfo(name = "playerID") var playerId: Long = 0,
    @ColumnInfo(name = "size") var size: Int = 0
)

internal fun RoomPlayerGamesCount.toPlayerGamesCount(): PlayerGamesCount {
    return PlayerGamesCount(playerId, size)
}
