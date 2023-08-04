package com.telen.easylineup.domain.model

data class RosterItem(
    val player: Player,
    var selected: Boolean,
    var playerNumberOverlay: PlayerNumberOverlay?
)

fun RosterItem.toRosterPlayerStatus() : RosterPlayerStatus {
    return RosterPlayerStatus(player, selected, playerNumberOverlay)
}