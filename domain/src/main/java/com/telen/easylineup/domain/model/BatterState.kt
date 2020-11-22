package com.telen.easylineup.domain.model

data class BatterState(val playerID: Long, val playerFlag: Int, var playerOrder: Int, val playerName: String, val playerNumber: String,
                       val playerPosition: FieldPosition, val playerPositionDesc: String, val canShowPosition: Boolean,
                       val canMove: Boolean, val canShowDescription: Boolean, val canShowOrder: Boolean, val origin: PlayerWithPosition) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BatterState

        if (playerID != other.playerID) return false
        if (playerFlag != other.playerFlag) return false
        if (playerOrder != other.playerOrder) return false
        if (playerName != other.playerName) return false
        if (playerNumber != other.playerNumber) return false
        if (playerPosition != other.playerPosition) return false
        if (playerPositionDesc != other.playerPositionDesc) return false
        if (canShowPosition != other.canShowPosition) return false
        if (canMove != other.canMove) return false
        if (canShowDescription != other.canShowDescription) return false
        if (canShowOrder != other.canShowOrder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerID.hashCode()
        result = 31 * result + playerFlag
        result = 31 * result + playerOrder
        result = 31 * result + playerName.hashCode()
        result = 31 * result + playerNumber.hashCode()
        result = 31 * result + playerPosition.hashCode()
        result = 31 * result + playerPositionDesc.hashCode()
        result = 31 * result + canShowPosition.hashCode()
        result = 31 * result + canMove.hashCode()
        result = 31 * result + canShowDescription.hashCode()
        result = 31 * result + canShowOrder.hashCode()
        return result
    }
}