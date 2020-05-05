package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PlayerInLineup

internal data class RoomPlayerInLineup(
        @ColumnInfo(name = "lineupName") var lineupName: String = "",
        @ColumnInfo(name = "lineupID") var lineupID: Long = 0,
        @ColumnInfo(name = "position") var position: Int?,
        @ColumnInfo(name = "playerName") var playerName: String?,
        @ColumnInfo(name = "playerID") var playerID: Long?
)

internal fun RoomPlayerInLineup.toPlayerInLineup(): PlayerInLineup {
    return PlayerInLineup(lineupName, lineupID, position, playerName, playerID)
}