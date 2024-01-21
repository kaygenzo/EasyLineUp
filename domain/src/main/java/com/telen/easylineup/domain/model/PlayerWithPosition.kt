/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

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
data class PlayerWithPosition(
    val playerName: String,
    val playerSex: Int,
    var shirtNumber: Int,
    val licenseNumber: Long,
    val teamId: Long,
    val image: String?,
    var position: Int = -1,
    var x: Float = 0f,
    var y: Float = 0f,
    var flags: Int = 0,
    var order: Int = 0,
    var fieldPositionId: Long = 0,
    var playerId: Long = 0,
    val lineupId: Long,
    val playerPositions: Int
)

fun PlayerWithPosition.toPlayer(): Player {
    return Player(
        id = playerId,
        teamId = teamId,
        name = playerName,
        shirtNumber = shirtNumber,
        licenseNumber = licenseNumber,
        image = image,
        positions = playerPositions,
        sex = playerSex
    )
}

fun PlayerWithPosition.toPlayerFieldPosition(): PlayerFieldPosition {
    return PlayerFieldPosition(
        id = fieldPositionId, playerId = playerId, position = position, x = x, y = y,
        order = order, lineupId = lineupId, flags = flags
    )
}

fun PlayerWithPosition.isSubstitute(): Boolean {
    return position == FieldPosition.SUBSTITUTE.id ||
            (position == FieldPosition.OLD_SUBSTITUTE.id && fieldPositionId > 0)
}

fun PlayerWithPosition.isAssigned(): Boolean {
    return position > 0 || (position == FieldPosition.OLD_SUBSTITUTE.id && fieldPositionId > 0)
}

fun PlayerWithPosition.isBatter(): Boolean {
    return isAssigned() && order > 0
}

fun PlayerWithPosition.isFirstBase(): Boolean {
    return position == FieldPosition.FIRST_BASE.id
}

fun PlayerWithPosition.isShortStop(): Boolean {
    return position == FieldPosition.SHORT_STOP.id
}

fun PlayerWithPosition.isRightField(): Boolean {
    return position == FieldPosition.RIGHT_FIELD.id
}

fun PlayerWithPosition.isPitcher(): Boolean {
    return position == FieldPosition.PITCHER.id
}

fun PlayerWithPosition.isCatcher(): Boolean {
    return position == FieldPosition.CATCHER.id
}

fun PlayerWithPosition.isDpDh(): Boolean {
    return position == FieldPosition.DP_DH.id
}

fun PlayerWithPosition.isFlex(): Boolean {
    return flags and PlayerFieldPosition.FLAG_FLEX > 0
}

fun PlayerWithPosition.isDpDhOrFlex(): Boolean {
    return isDpDh() || isFlex()
}

fun PlayerWithPosition.isDefensePlayer(): Boolean {
    return !isSubstitute() && !isDpDh() && isAssigned()
}

fun PlayerWithPosition.reset() {
    this.position = -1
    this.order = 0
    this.flags = PlayerFieldPosition.FLAG_NONE
}

// here we exclude the position itself because it's also a candidate for the order.
// For instance, if the order is 1, order 1 is a new candidate as well as others because it
// will be a free order
fun List<PlayerWithPosition>.getNextAvailableOrder(excludedOrders: List<Int>? = null): Int {
    var availableOrder = 1
    this.filter { it.isBatter() && !(excludedOrders?.contains(it.order) ?: false) }
        .sortedBy { it.order }
        .forEach {
            if (it.order == availableOrder) {
                availableOrder++
            } else {
                return availableOrder
            }
        }
    return availableOrder
}
