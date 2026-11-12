/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PlayerInLineup

/**
 * @property lineupName
 * @property lineupId
 * @property position
 * @property playerName
 * @property playerId
 */
internal data class RoomPlayerInLineup(
    @ColumnInfo(name = "lineupName") val lineupName: String = "",
    @ColumnInfo(name = "lineupID") val lineupId: Long = 0,
    @ColumnInfo(name = "position") val position: Int?,
    @ColumnInfo(name = "playerName") val playerName: String?,
    @ColumnInfo(name = "playerID") val playerId: Long?
)

internal fun RoomPlayerInLineup.toDomain(): PlayerInLineup {
    return PlayerInLineup(
        lineupName = lineupName,
        lineupId = lineupId,
        position = position,
        playerName = playerName,
        playerId = playerId
    )
}
