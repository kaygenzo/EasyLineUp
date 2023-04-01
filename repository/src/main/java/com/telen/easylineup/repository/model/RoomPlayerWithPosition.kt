package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import com.telen.easylineup.domain.model.PlayerWithPosition

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
    @ColumnInfo(name = "fieldPositionID") var fieldPositionID: Long = 0,
    @ColumnInfo(name = "playerID") val playerID: Long,
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
        fieldPositionID,
        playerID,
        lineupId,
        playerPositions
    )
}