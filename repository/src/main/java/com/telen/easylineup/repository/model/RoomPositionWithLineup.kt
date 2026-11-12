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
    @ColumnInfo(name = "position") val position: Int = 0,
    @ColumnInfo(name = "x") val x: Float = 0f,
    @ColumnInfo(name = "y") val y: Float = 0f,
    @ColumnInfo(name = "order") val order: Int = 0,
    @ColumnInfo(name = "lineupName") val lineupName: String = "",
    @ColumnInfo(name = "tournamentName") val tournamentName: String = ""
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
