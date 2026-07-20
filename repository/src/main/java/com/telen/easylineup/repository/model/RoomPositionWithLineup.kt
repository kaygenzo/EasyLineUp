/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PositionWithLineup

/**
 * @property position
 * @property x
 * @property y
 * @property order
 * @property lineupName
 * @property tournamentName
 */
internal data class RoomPositionWithLineup(
    @ColumnInfo(name = "position") var position: Int = 0,
    @ColumnInfo(name = "x") var x: Float = 0f,
    @ColumnInfo(name = "y") var y: Float = 0f,
    @ColumnInfo(name = "order") var order: Int = 0,
    @ColumnInfo(name = "lineupName") var lineupName: String = "",
    @ColumnInfo(name = "tournamentName") var tournamentName: String = ""
)

internal fun RoomPositionWithLineup.toDomain(): PositionWithLineup {
    return PositionWithLineup(
        position = position,
        x = x,
        y = y,
        order = order,
        lineupName = lineupName,
        tournamentName = tournamentName
    )
}
