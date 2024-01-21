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
    @ColumnInfo(name = "lineupName") var lineupName: String = "",
    @ColumnInfo(name = "lineupID") var lineupId: Long = 0,
    @ColumnInfo(name = "position") var position: Int?,
    @ColumnInfo(name = "playerName") var playerName: String?,
    @ColumnInfo(name = "playerID") var playerId: Long?
)

internal fun RoomPlayerInLineup.toPlayerInLineup(): PlayerInLineup {
    return PlayerInLineup(lineupName, lineupId, position, playerName, playerId)
}
