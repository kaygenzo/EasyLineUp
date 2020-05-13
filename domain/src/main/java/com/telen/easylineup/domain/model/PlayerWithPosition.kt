package com.telen.easylineup.domain.model

data class PlayerWithPosition(
        val playerName: String,
        val shirtNumber: Int,
        val licenseNumber: Long,
        val teamId: Long,
        val image: String?,
        var position: Int = 0,
        var x: Float = 0f,
        var y: Float = 0f,
        var flags: Int = 0,
        var order: Int = 0,
        var fieldPositionID: Long = 0,
        var playerID: Long = 0,
        val lineupId: Long,
        val playerPositions: Int
) {

    companion object {
        // here we exclude the position itself because it's also a candidate for the order.
        // For instance, if the order is 1, order 1 is a new candidate as well as others because it will
        // be a free order
        fun getNextAvailableOrder(players: List<PlayerWithPosition>, exclude: List<Int>? = null): Int {
            var availableOrder = 1
            players
                    .filter { it.fieldPositionID > 0 && it.order > 0 && !(exclude?.contains(it.order) ?: false)}
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