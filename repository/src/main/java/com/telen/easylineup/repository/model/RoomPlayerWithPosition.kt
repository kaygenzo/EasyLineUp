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
 * @property fieldPositionHash
 * @property playerId
 * @property lineupId
 * @property playerPositions
 */
internal data class RoomPlayerWithPosition(
    @ColumnInfo(name = "playerName") val playerName: String,
    @ColumnInfo(name = "playerSex") val playerSex: Int,
    @ColumnInfo(name = "shirtNumber") val shirtNumber: Int,
    @ColumnInfo(name = "licenseNumber") val licenseNumber: String,
    @ColumnInfo(name = "teamID") val teamId: Long,
    @ColumnInfo(name = "image") val image: String?,
    @ColumnInfo(name = "position") val position: Int = -1,
    @ColumnInfo(name = "x") val x: Float = 0f,
    @ColumnInfo(name = "y") val y: Float = 0f,
    @ColumnInfo(name = "flags") val flags: Int = 0,
    @ColumnInfo(name = "order") val order: Int = 0,
    @ColumnInfo(name = "fieldPositionID") val fieldPositionId: Long = 0,
    @ColumnInfo(name = "fieldPositionHash") val fieldPositionHash: String? = null,
    @ColumnInfo(name = "playerID") val playerId: Long,
    @ColumnInfo(name = "lineupID") val lineupId: Long,
    @ColumnInfo(name = "playerPositions") val playerPositions: Int
)

internal fun RoomPlayerWithPosition.toDomain(): PlayerWithPosition {
    return PlayerWithPosition(
        playerName = playerName,
        playerSex = playerSex,
        shirtNumber = shirtNumber,
        licenseNumber = licenseNumber,
        teamId = teamId,
        image = image,
        position = position,
        x = x,
        y = y,
        flags = flags,
        order = order,
        fieldPositionId = fieldPositionId,
        fieldPositionHash = fieldPositionHash,
        playerId = playerId,
        lineupId = lineupId,
        playerPositions = playerPositions
    )
}
