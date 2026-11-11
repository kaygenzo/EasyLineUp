/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.Constants

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
    val shirtNumber: Int,
    val licenseNumber: String,
    val teamId: Long,
    val image: String?,
    val position: Int = -1,
    val x: Float = 0f,
    val y: Float = 0f,
    val flags: Int = 0,
    val order: Int = 0,
    val fieldPositionId: Long = 0,
    val playerId: Long = 0,
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

/**
 * Whether this substitute currently holds a spot in the batting order (as opposed to
 * bench-only).
 */
fun PlayerWithPosition.isBattingSubstitute(): Boolean {
    return isSubstitute() && order in 1 until Constants.SUBSTITUTE_ORDER_VALUE
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

/** This player sent back to the bench: a substitute, out of the batting order, no flag. */
fun PlayerWithPosition.reset(): PlayerWithPosition = copy(
    position = FieldPosition.SUBSTITUTE.id,
    order = 0,
    flags = PlayerFieldPosition.FLAG_NONE
)

// Unlike reset() (field player -> bench), this fully removes a player who was already a
// substitute: position goes to the model's own "not assigned" default (-1), which makes
// SaveBattingOrderAndPositions delete the row instead of writing it back as SUBSTITUTE.
fun PlayerWithPosition.unassign(): PlayerWithPosition = copy(
    position = -1,
    order = 0,
    flags = PlayerFieldPosition.FLAG_NONE
)

/**
 * Replaces the element at [index] by [transform] applied to it, and returns the new element. The
 * lineup use cases edit a working copy of the draft through this, one step at a time, so that
 * what they read from the list between two steps is what the list holds at that point.
 */
internal inline fun <T> MutableList<T>.update(index: Int, transform: (T) -> T): T {
    val updated = transform(this[index])
    this[index] = updated
    return updated
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
