package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo

data class PlayerWithPosition(
        @ColumnInfo(name = "playerName") val playerName: String,
        @ColumnInfo(name = "shirtNumber") val shirtNumber: Int,
        @ColumnInfo(name = "licenseNumber") val licenseNumber: Long,
        @ColumnInfo(name = "teamID") val teamId: Long,
        @ColumnInfo(name = "image") val image: String?,
        @ColumnInfo(name = "position") var position: Int = 0,
        @ColumnInfo(name = "x") var x: Float = 0f,
        @ColumnInfo(name = "y") var y: Float = 0f,
        @ColumnInfo(name = "flags") var flags: Int = 0,
        @ColumnInfo(name = "order") var order: Int = 0,
        @ColumnInfo(name = "fieldPositionID") var fieldPositionID: Long = 0,
        @ColumnInfo(name = "playerID") val playerID: Long,
        @ColumnInfo(name = "lineupID") val lineupId: Long,
        @ColumnInfo(name = "playerPositions") val playerPositions: Int
) {

    companion object {
        fun getNextAvailableOrder(players: List<PlayerWithPosition>): Int {
            var availableOrder = 1
            players
                    .filter { it.fieldPositionID > 0 && it.order > 0 }
                    .sortedBy { it.order }
                    .forEach {
                        if(it.order == availableOrder)
                            availableOrder++
                        else
                            return availableOrder
                    }
            return availableOrder
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerWithPosition

        if (playerName != other.playerName) return false
        if (shirtNumber != other.shirtNumber) return false
        if (licenseNumber != other.licenseNumber) return false
        if (teamId != other.teamId) return false
        if (image != other.image) return false
        if (position != other.position) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (flags != other.flags) return false
        if (order != other.order) return false
        if (fieldPositionID != other.fieldPositionID) return false
        if (playerID != other.playerID) return false
        if (lineupId != other.lineupId) return false
        if (playerPositions != other.playerPositions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerName.hashCode()
        result = 31 * result + shirtNumber
        result = 31 * result + licenseNumber.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + position
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + order
        result = 31 * result + fieldPositionID.hashCode()
        result = 31 * result + playerID.hashCode()
        result = 31 * result + lineupId.hashCode()
        result = 31 * result + playerPositions
        result = 31 * result + flags
        return result
    }
}

fun PlayerWithPosition.toPlayer(): Player {
    return Player(id = playerID, teamId = teamId, name = playerName, shirtNumber = shirtNumber, licenseNumber = licenseNumber, image = image, positions = playerPositions)
}

fun PlayerWithPosition.toPlayerFieldPosition(): PlayerFieldPosition {
    return PlayerFieldPosition(id = fieldPositionID, playerId = playerID, position = position, x = x, y = y, order = order, lineupId = lineupId, flags = flags)
}