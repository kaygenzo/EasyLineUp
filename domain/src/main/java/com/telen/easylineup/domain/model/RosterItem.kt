/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property player
 * @property selected
 * @property playerNumberOverlay
 */
data class RosterItem(
    val player: Player,
    var selected: Boolean,
    var playerNumberOverlay: PlayerNumberOverlay?
)

fun RosterItem.toRosterPlayerStatus(): RosterPlayerStatus {
    return RosterPlayerStatus(player, selected, playerNumberOverlay)
}
