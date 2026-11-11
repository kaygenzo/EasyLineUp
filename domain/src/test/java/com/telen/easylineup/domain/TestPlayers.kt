/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerWithPosition

/**
 * Replaces the first player matching [predicate] by [transform] applied to it: how a fixture
 * changes one player of a list now that [PlayerWithPosition] is immutable.
 */
internal fun MutableList<PlayerWithPosition>.edit(
    predicate: (PlayerWithPosition) -> Boolean,
    transform: PlayerWithPosition.() -> PlayerWithPosition
) {
    val index = indexOfFirst(predicate)
    require(index >= 0) { "No player matches" }
    this[index] = this[index].transform()
}

/** Replaces the player at [index] by [transform] applied to it. */
internal fun MutableList<PlayerWithPosition>.edit(
    index: Int,
    transform: PlayerWithPosition.() -> PlayerWithPosition
) {
    this[index] = this[index].transform()
}
