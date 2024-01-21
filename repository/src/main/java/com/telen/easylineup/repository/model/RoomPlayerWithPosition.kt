/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PlayerWithPosition

/**
 * @property playerName
 * @property playerSex
 * @property shirtNumber
 * @property licenseNumber
 * @property teamId
 * @property image
 * @property position
 * @property x
 * @property y
 * @property flags
 * @property order
 * @property fieldPositionId
 * @property playerId
 * @property lineupId
 * @property playerPositions
 */
internal data class RoomPlayerWithPosition(
    @ColumnInfo(name = "playerName") val playerName: String,
    @ColumnInfo(name = "playerSex") val playerSex: Int,
    @ColumnInfo(name = "shirtNumber") val shirtNumber: Int,
    @ColumnInfo(name = "licenseNumber") val licenseNumber: Long,
    @ColumnInfo(name = "teamID") val teamId: Long,
    @ColumnInfo(name = "image") val image: String?,
    @ColumnInfo(name = "position") var position: Int = -1,
    @ColumnInfo(name = "x") var x: Float = 0f,
    @ColumnInfo(name = "y") var y: Float = 0f,
    @ColumnInfo(name = "flags") var flags: Int = 0,
    @ColumnInfo(name = "order") var order: Int = 0,
    @ColumnInfo(name = "fieldPositionID") var fieldPositionId: Long = 0,
    @ColumnInfo(name = "playerID") val playerId: Long,
    @ColumnInfo(name = "lineupID") val lineupId: Long,
    @ColumnInfo(name = "playerPositions") val playerPositions: Int
)

internal fun RoomPlayerWithPosition.toPlayerWithPosition(): PlayerWithPosition {
    return PlayerWithPosition(
        playerName,
        playerSex,
        shirtNumber,
        licenseNumber,
        teamId,
        image,
        position,
        x,
        y,
        flags,
        order,
        fieldPositionId,
        playerId,
        lineupId,
        playerPositions
    )
}
